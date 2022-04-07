package org.jetbrains.plugins.scala
package debugger
package evaluation

import org.jetbrains.plugins.scala.util.runners.{MultipleScalaVersionsRunner, RunWithScalaVersions, TestScalaVersion}
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(classOf[MultipleScalaVersionsRunner])
@RunWithScalaVersions(Array(
  TestScalaVersion.Scala_2_11,
  TestScalaVersion.Scala_2_12,
  TestScalaVersion.Scala_2_13
))
@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_2 extends LocalVariablesEvaluationTestBase

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_3_0 extends LocalVariablesEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testLocalObjectOuter(): Unit = {}

  override def testLocalMethodLocalObject(): Unit = {}
}

@Category(Array(classOf[DebuggerTests]))
class LocalVariablesEvaluationTest_3_1 extends LocalVariablesEvaluationTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class LocalVariablesEvaluationTestBase extends NewScalaDebuggerTestCase {
  def testLocal(): Unit = {
    createLocalProcess("Local")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testLocalClassParam(): Unit = {
    createLocalProcess("LocalClassParam")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testClassParamInConstructor(): Unit = {
    createLocalProcess("ClassParamInConstructor")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "unused".evaluateAsInt)
      assertEquals(2, "used".evaluateAsInt)
      assertEquals(3, "field".evaluateAsInt)
    }
  }

  def testBackingFieldParamInMethod(): Unit = {
    createLocalProcess("BackingFieldParamInMethod")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "backingField".evaluateAsInt)
    }
  }

  def testBackingFieldParamInConstructor(): Unit = {
    createLocalProcess("BackingFieldParamInConstructor")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "backingField".evaluateAsInt)
    }
  }

  def testNoBackingFieldParam(): Unit = {
    createLocalProcess("NoBackingFieldParam")

    doWhenPausedThenResume { implicit context =>
      assertEquals("constructor parameter 'noBackingField' is inaccessible outside of the class constructor", interceptEvaluationException("noBackingField"))
    }
  }

  def testLocalFromForStatement(): Unit = {
    createLocalProcess("LocalFromForStatement")

    doWhenPausedThenResume { implicit context =>
      assertEquals("1", "x".evaluateAsString)
    }
  }

  def testLocalFromForStatementFromOut(): Unit = {
    createLocalProcess("LocalFromForStatementFromOut")

    doWhenPausedThenResume { implicit context =>
      assertEquals("1", "x".evaluateAsString)
    }
  }

  def testParam(): Unit = {
    createLocalProcess("Param")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testLocalParam(): Unit = {
    createLocalProcess("LocalParam")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testLocalOuter(): Unit = {
    createLocalProcess("LocalOuter")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testLocalOuterOuter(): Unit = {
    createLocalProcess("LocalOuterOuter")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
      assertEquals("a", "y".evaluateAsString)
    }
  }

  def testLocalObjectOuter(): Unit = {
    createLocalProcess("LocalObjectOuter")

    doWhenPausedThenResume { implicit context =>
      assertStartsWith("LocalObjectOuter$x", "x".evaluateAsString)
    }
  }

  def testLocalFromClosureAndClass(): Unit = {
    createLocalProcess("LocalFromClosureAndClass")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
      assertEquals("a", "y".evaluateAsString)
    }
  }

  def testLocalMethodLocal(): Unit = {
    createLocalProcess("LocalMethodLocal")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
      assertEquals("a", "s".evaluateAsString)
    }
  }

  def testLocalMethodLocalObject(): Unit = {
    createLocalProcess("LocalMethodLocalObject")

    doWhenPausedThenResume { implicit context =>
      assertStartsWith("LocalMethodLocalObject$x", "x".evaluateAsString)
    }
  }

  def testLocalMethodLocalMethodLocal(): Unit = {
    createLocalProcess("LocalMethodLocalMethodLocal")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
      assertEquals("a", "s".evaluateAsString)
    }
  }

  def testLocalMethodLocalMethodLocalClass(): Unit = {
    createLocalProcess("LocalMethodLocalMethodLocalClass")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
      assertEquals("a", "s".evaluateAsString)
    }
  }

  def testLocalMethodLocalMethodLocalClassLocalMethod(): Unit = {
    createLocalProcess("LocalMethodLocalMethodLocalClassLocalMethod")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testLocalObjectInside(): Unit = {
    createLocalProcess("LocalObjectInside")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
    }
  }

  def testLocalObjectInsideClassLevel(): Unit = {
    createLocalProcess("LocalObjectInsideClassLevel")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x".evaluateAsInt)
      assertEquals("a", "s".evaluateAsString)
    }
  }

  def testLocalUseNamedArgs(): Unit = {
    createLocalProcess("LocalUseNamedArgs")

    doWhenPausedThenResume { implicit context =>
      assertEquals(3, "inner()".evaluateAsInt)
      assertEquals(4, "inner(2)".evaluateAsInt)
      assertEquals(3, "inner2()".evaluateAsInt)
      assertEquals(3, "inner2".evaluateAsInt)
    }
  }
}
