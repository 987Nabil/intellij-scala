package org.jetbrains.plugins.scala.lang.formatting.scalafmt

import com.intellij.openapi.project.{Project, ProjectManagerListener}
import com.intellij.openapi.startup.StartupActivity

class ScalafmtProjectListener extends StartupActivity with ProjectManagerListener {

  override def runActivity(project: Project): Unit = {
    ScalaFmtSuggesterService.instance(project).init()
    ScalafmtDynamicConfigService.instanceIn(project).init()
  }

  override def projectClosing(project: Project): Unit =
    ScalafmtDynamicConfigService.instanceIn(project).clearCaches()
}
