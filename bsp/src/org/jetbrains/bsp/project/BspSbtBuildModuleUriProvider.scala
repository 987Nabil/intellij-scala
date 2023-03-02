package org.jetbrains.bsp.project

import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import org.jetbrains.bsp.BSP
import org.jetbrains.bsp.data.SbtModuleDataBsp
import org.jetbrains.sbt.ExternalSystemUtil
import org.jetbrains.sbt.project.SbtBuildModuleUriProvider

import java.net.URI

final class BspSbtBuildModuleUriProvider extends SbtBuildModuleUriProvider {

  override def getBuildModuleUri(module: Module): Option[URI] = {
    val sbtModuleData = getSbtModuleData(module)
    sbtModuleData.map(_.buildModuleId.uri)
  }

  private def getSbtModuleData(module: Module): Option[SbtModuleDataBsp] = {
    val project = module.getProject
    val moduleId = ExternalSystemApiUtil.getExternalProjectId(module) // nullable, but that's okay for use in predicate
    getSbtModuleData(project, moduleId)
  }

  private def getSbtModuleData(project: Project, moduleId: String): Option[SbtModuleDataBsp] = {
    val emptyURI = new URI("")

    val moduleDataSeq = getModuleData(project, moduleId, SbtModuleDataBsp.Key)
    moduleDataSeq.find(_.id.uri != emptyURI)
  }

  private def getModuleData[K](project: Project, moduleId: String, key: Key[K]): Iterable[K] = {
    val dataEither = ExternalSystemUtil.getModuleData(BSP.ProjectSystemId, project, moduleId, key)
    //TODO: do we need to report the warning to user
    // However there is some code which doesn't expect the data to be present and just checks if it exists
    // So before reporting the warning to user we need to review usage code and decide which code expects
    // the data and which not and then probably split API into two versions: something like "get" and "getOptional"...
    dataEither.getOrElse(Nil)
  }
}