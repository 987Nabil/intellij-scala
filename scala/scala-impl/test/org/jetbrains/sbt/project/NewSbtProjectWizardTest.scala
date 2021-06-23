package org.jetbrains.sbt.project

import com.intellij.ide.projectWizard.ProjectSettingsStep
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.ScalaVersion
import org.jetbrains.sbt.project.ProjectStructureDsl._
import org.jetbrains.sbt.project.template.SbtModuleBuilder

class NewSbtProjectWizardTest extends NewScalaProjectTestCase with ExactMatch {

  override protected def setUp(): Unit = {
    super.setUp()
    configureJdk()
  }

  def testCreateProjectWithLowerCaseName(): Unit =
    runCreateSbtProjectTest("lower-case-project-name")

  def testCreateProjectWithDotsInNameName(): Unit =
    runCreateSbtProjectTest("project.name.with.dots")

  def testCreateProjectWithUpperCaseName_LowerCaseFirstLetter(): Unit =
    runCreateSbtProjectTest("lowerCaseFirstLetterProjectName")

  def testCreateProjectWithUpperCaseName_UpperCaseFirstLetter(): Unit =
    runCreateSbtProjectTest("UpperCaseFirstLetterProjectName")

  private def runCreateSbtProjectTest(projectName: String): Unit = {
    val scalaVersion = ScalaVersion.fromString("2.13.6").get
    val sbtVersion = SbtModuleBuilder.LatestSbtVersion

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
          excluded := Seq("target")
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
      scalaVersion.minor,
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
        val settingsStep = projectSettingsStep.getSettingsStepTyped[SbtModuleBuilder#MySdkSettingsStep]
        settingsStep.setScalaVersion(scalaVersion)
        settingsStep.setSbtVersion(sbtVersion)
      case _ =>
    }

    assertProjectsEqual(expectedProject, project)
  }
}
