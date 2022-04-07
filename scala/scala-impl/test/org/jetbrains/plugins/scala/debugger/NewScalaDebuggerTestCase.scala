package org.jetbrains.plugins.scala
package debugger

import com.intellij.debugger.engine.evaluation.{CodeFragmentKind, EvaluateException}
import com.intellij.debugger.engine.{DebuggerUtils, SuspendContextImpl}
import com.intellij.debugger.impl.{DescriptorTestCase, OutputChecker}
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.execution.configurations.JavaParameters
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.EdtTestUtil
import com.sun.jdi._
import org.jetbrains.plugins.scala.base.ScalaSdkOwner
import org.jetbrains.plugins.scala.base.libraryLoaders._
import org.jetbrains.plugins.scala.compilation.CompilerTestUtil
import org.jetbrains.plugins.scala.compilation.CompilerTestUtil.RevertableChange
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiManager
import org.jetbrains.plugins.scala.project._
import org.jetbrains.plugins.scala.util.TestUtils
import org.junit.Assert.fail

import java.io._
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import scala.util.{Try, Using}

abstract class NewScalaDebuggerTestCase extends DescriptorTestCase with ScalaSdkOwner {

  private val compilerConfig: RevertableChange = CompilerTestUtil.withEnabledCompileServer(false)

  private val baseDebuggerPath: Path = Path.of(TestUtils.getTestDataPath, "debugger")

  private val testAppPath: Path = baseDebuggerPath.resolve("tests")

  private val srcPath: Path = testAppPath.resolve("src")

  private val outputPath: Path = baseDebuggerPath.resolve("testsOut")

  private lazy val versionSpecific: Path = Path.of(s"scala-${version.major}")

  private lazy val classesOutputPath: Path = outputPath.resolve(versionSpecific).resolve("classes")

  private lazy val checksumsPath: Path = outputPath.resolve(versionSpecific).resolve("checksums")

  private lazy val checksumsFilePath: Path = checksumsPath.resolve("checksums.dat")

  override protected def initOutputChecker(): OutputChecker =
    new OutputChecker(() => getTestAppPath, () => getAppOutputPath) {
      override def checkValid(jdk: Sdk, sortClassPath: Boolean): Unit = {}
    }

  override protected def getTestAppPath: String = testAppPath.toString

  override protected def getModuleOutputDir: Path = classesOutputPath

  override protected def getAppOutputPath: String = getModuleOutputDir.toString

  override def testProjectJdkVersion: LanguageLevel = LanguageLevel.JDK_11

  override protected def getTestProjectJdk: Sdk = SmartJDKLoader.getOrCreateJDK(testProjectJdkVersion)

  override protected def librariesLoaders: Seq[LibraryLoader] = Seq(
    ScalaSDKLoader(includeScalaReflectIntoCompilerClasspath = true),
    HeavyJDKLoader(testProjectJdkVersion),
    SourcesLoader(srcPath.toString)
  )

  override protected def setUpModule(): Unit = {
    super.setUpModule()
    EdtTestUtil.runInEdtAndWait { () =>
      setUpLibraries(getModule)
      compilerConfig.applyChange()
    }
  }

  override protected def setUp(): Unit = {
    Files.createDirectories(classesOutputPath)
    Files.createDirectories(checksumsPath)

    super.setUp()

    compileProject()

    srcPath.toFile.listFiles().foreach { f =>
      VfsUtil.findFile(f.toPath, true)
    }
  }

  override protected def initApplication(): Unit = {
    super.initApplication()
    NodeRendererSettings.getInstance().getClassRenderer.SHOW_DECLARED_TYPE = false
  }

  override protected def tearDown(): Unit = {
    try {
      EdtTestUtil.runInEdtAndWait { () =>
        compilerConfig.revertChange()
        disposeLibraries(getModule)
      }
    } finally {
      super.tearDown()
    }
  }

  override protected def compileProject(): Unit = {
    def loadChecksumsFromDisk(): Map[Path, Array[Byte]] =
      Using(new ObjectInputStream(new FileInputStream(checksumsFilePath.toFile)))(_.readObject())
        .map(_.asInstanceOf[Map[String, Array[Byte]]])
        .map(_.map { case (path, checksum) => (Path.of(path), checksum) })
        .getOrElse(Map.empty)

    val messageDigest = MessageDigest.getInstance("MD5")

    def calculateSrcCheksums(): Map[Path, Array[Byte]] = {
      def checksum(file: File): Array[Byte] = {
        val fileBytes = Files.readAllBytes(file.toPath)
        messageDigest.digest(fileBytes)
      }

      def checksumsInDir(dir: File): List[(Path, Array[Byte])] =
        dir.listFiles().toList.flatMap { f =>
          if (f.isDirectory) checksumsInDir(f) else List((f.toPath, checksum(f)))
        }

      checksumsInDir(srcPath.toFile).toMap
    }

    def shouldCompile(srcChecksums: Map[Path, Array[Byte]], diskChecksums: Map[Path, Array[Byte]]): Boolean =
      !srcChecksums.forall { case (srcPath, srcSum) =>
        diskChecksums.get(srcPath).exists(java.util.Arrays.equals(srcSum, _))
      }

    def writeChecksumsToDisk(checksums: Map[Path, Array[Byte]]): Unit = {
      val strings = checksums.map { case (path, sum) => (path.toString, sum) }
      Using(new ObjectOutputStream(new FileOutputStream(checksumsFilePath.toFile)))(_.writeObject(strings))
    }

    val srcChecksums = calculateSrcCheksums()

    val compareChecksums = for {
      diskChecksums <- Try(loadChecksumsFromDisk())
    } yield shouldCompile(srcChecksums, diskChecksums)

    val needsCompilation = compareChecksums.getOrElse(true)

    if (needsCompilation) {
      super.compileProject()
      writeChecksumsToDisk(srcChecksums)
    }
  }

  override protected def createJavaParameters(mainClass: String): JavaParameters = {
    val params = new JavaParameters()
    params.getClassPath.addAllFiles(getModule.scalaCompilerClasspath.toArray)
    params.getClassPath.add(getAppOutputPath)
    params.setJdk(getTestProjectJdk)
    params.setWorkingDirectory(getTestAppPath)
    params.setMainClass(mainClass)
    params
  }

  override protected def createBreakpoints(className: String): Unit = {
    if (!classesOutputPath.resolve(s"$className.class").toFile.exists()) {
      fail(s"Could not find compiled class $className")
    }

    val manager = ScalaPsiManager.instance(getProject)
    val psiClass = inReadAction(manager.getCachedClass(GlobalSearchScope.allScope(getProject), className))
    val psiFile = psiClass.map(_.getContainingFile).getOrElse(throw new AssertionError(s"Could not find main class $className"))

    createBreakpoints(psiFile)
  }

  protected def interceptEvaluationException(expression: String)(implicit context: SuspendContextImpl): String = {
    try {
      expression.evaluateAs[Value]
      throw new AssertionError(s"Evaluating expression $expression was supposed to fail, but didn't")
    } catch {
      case e: EvaluateException => e.getMessage
    }
  }

  protected implicit class EvaluateAsOps(expression: String) {
    def evaluateAs[V <: Value](implicit context: SuspendContextImpl): V =
      evaluate(CodeFragmentKind.EXPRESSION, expression, context).asInstanceOf[V]

    def evaluateAsString(implicit context: SuspendContextImpl): String = {
      val ec = createEvaluationContext(context)
      DebuggerUtils.getValueAsString(ec, evaluateAs[ObjectReference])
    }

    def evaluateAsInt(implicit context: SuspendContextImpl): Int =
      evaluateAs[IntegerValue].value()

    def evaluateAsBoolean(implicit context: SuspendContextImpl): Boolean =
      evaluateAs[BooleanValue].value()
  }

  protected def assertEquals[A, B](expected: A, actual: B)(implicit ev: A <:< B): Unit = {
    org.junit.Assert.assertEquals(expected, actual)
  }

  protected def assertEquals(expected: Int, actual: Int): Unit = {
    org.junit.Assert.assertEquals(expected, actual)
  }

  protected def assertStartsWith(expected: String, actual: String): Unit = {
    if (!actual.startsWith(expected)) {
      fail(s""""$actual" does not start with "$expected"""")
    }
  }

  protected def voidValue(): VoidValue = getDebugProcess.getVirtualMachineProxy.mirrorOfVoid()
}
