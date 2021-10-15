package org.jetbrains.sbt.project

import com.intellij.ide.projectWizard.ProjectSettingsStep
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.project.Versions
import org.jetbrains.sbt.project.ProjectStructureDsl._
import org.jetbrains.sbt.project.template.SbtModuleBuilder

class NewSbtProjectWizardTest extends NewScalaProjectWizardTestBase with ExactMatch {

  override protected def setUp(): Unit = {
    super.setUp()
    configureJdk()
  }

  def testCreateProjectWithLowerCaseName(): Unit =
    runCreateSbtProjectTest("lowe_case_project_name")

  def testCreateProjectWithUpperCaseName(): Unit =
    runCreateSbtProjectTest("UpperCaseProjectName")
  
  //SCL-12528, SCL-12528
  def testCreateProjectWithDotsSpacesAndDashesInNameName(): Unit =
    runCreateSbtProjectTest("project.name.with.dots spaces and-dashes and UPPERCASE")

  private def runCreateSbtProjectTest(projectName: String): Unit = {
    val scalaVersion = "2.13.6"
    val sbtVersion = Versions.SBT.LatestSbtVersion

    //noinspection TypeAnnotation
    val expectedProject = new project(projectName) {
      lazy val scalaLibrary = expectedScalaLibrary(scalaVersion)

      libraries := Seq(scalaLibrary)
      libraries.exactMatch()

      modules := Seq(
        new module(projectName) {
          libraryDependencies := Seq(scalaLibrary)
          sources := Seq("src/main/scala")
          testSources := Seq("src/test/scala")
          excluded := Seq("project/target", "target")
        },
        new module(s"$projectName-build") {
          // TODO: why `-build` module contains empty string? in UI the `project` folder is marked as `sources`.
          //  Is it some implicit IntelliJ behaviour?
          sources := Seq("")
          excluded := Seq("project/target", "target")
        }
      )
    }

    runCreateSbtProjectTest(
      projectName,
      scalaVersion,
      sbtVersion,
      expectedProject
    )
  }

  private def runCreateSbtProjectTest(
    projectName: String,
    scalaVersion: String,
    sbtVersion: String,
    expectedProject: project
  ): Unit = {
    val project: Project = createScalaProject(
      SbtProjectSystem.Id.getReadableName,
      projectName
    ) {
      case projectSettingsStep: ProjectSettingsStep =>
        val settingsStep = projectSettingsStep.getSettingsStepTyped[SbtModuleBuilder.Step]
        settingsStep.setScalaVersion(scalaVersion)
        settingsStep.setSbtVersion(sbtVersion)
      case _ =>
    }

    assertProjectsEqual(expectedProject, project)
  }
}
