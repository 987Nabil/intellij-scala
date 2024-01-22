package org.jetbrains.plugins.scala.util

import com.intellij.compiler.impl.CompilerUtil
import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.externalSystem.model.project.ExternalSystemSourceType
import com.intellij.openapi.externalSystem.model.{ProjectKeys, ProjectSystemId}
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.plugins.scala.project.ModuleExt

import java.io.File
import java.util.Collections
import scala.jdk.CollectionConverters._

object ExternalSystemVfsUtil {

  /**
   * Refreshes the source content roots and output paths of all modules in a project. Used by sbt shell and BSP after
   * building the project. As both sbt shell and BSP run the project build in an external process, the IDEA Virtual
   * File System needs to be manually notified which file system directories have new content and need to be refreshed.
   *
   * @note Common code used by `SbtProjectTaskRunnerImpl` and `BspProjectTaskRunner`. The purpose of this utility
   *       function is to avoid bugs by forgetting to update the code in two places. Apart from that, it has limited
   *       functionality.
   */
  // remove this if/when external system handles this refresh on its own
  def refreshRoots(project: Project, id: ProjectSystemId): Unit = {
    // simply refresh all the source roots to catch any generated files -- this MAY have a performance impact
    // in which case it might be necessary to receive the generated sources directly from sbt and refresh them (see BuildManager)
    val info = Option(ProjectDataManager.getInstance().getExternalProjectData(project, id, project.getBasePath))
    val allSourceRoots = info
      .map(info => ExternalSystemApiUtil.findAllRecursively(info.getExternalProjectStructure, ProjectKeys.CONTENT_ROOT))
      .getOrElse(Collections.emptyList())
    val generatedSourceRoots = allSourceRoots.asScala.flatMap { node =>
      val data = node.getData
      // bsp-side and sbt-side generated sources are still imported as regular sources
      val generated = data.getPaths(ExternalSystemSourceType.SOURCE_GENERATED).asScala
      val regular = data.getPaths(ExternalSystemSourceType.SOURCE).asScala
      generated ++ regular
    }.map(_.getPath).toSeq.distinct

    // Because we don't have an exact way of knowing which modules have been affected, we need to refresh the output
    // directories of all modules in the project. Otherwise, we run the risk that the Run Configuration order
    // enumerator will not see all output directories in the VFS and will not put them on the runtime classpath.
    // In Gradle, affected modules are collected using an injected Gradle script, which tracks which modules are
    // affected by a build command.
    // https://github.com/JetBrains/intellij-community/blob/bf3083ca66771e038eb1c64128b4e508f52acfad/plugins/gradle/java/src/execution/build/GradleProjectTaskRunner.java#L60
    val outputRoots = {
      val allModules = ModuleManager.getInstance(project).getModules.filterNot(_.hasBuildModuleType)
      CompilerPaths.getOutputPaths(allModules)
    }

    val toRefresh = generatedSourceRoots ++ outputRoots
    CompilerUtil.refreshOutputRoots(toRefresh.asJavaCollection)

    // This is most likely not necessary. Gradle only invokes `CompilerUtil.refreshOutputRoots`.
    // Recursively refreshing the output directories is comparatively expensive. It is enough for the Run Configuration
    // order enumerator to just refresh the output directories without their children, but we don't have tests in place
    // in order to be more confident in this change.
    // https://github.com/JetBrains/intellij-community/blob/bf3083ca66771e038eb1c64128b4e508f52acfad/plugins/gradle/java/src/execution/build/GradleProjectTaskRunner.java#L174-L176
    val toRefreshFiles = toRefresh.map(new File(_)).asJava
    LocalFileSystem.getInstance().refreshIoFiles(toRefreshFiles, true, true, null)
  }
}