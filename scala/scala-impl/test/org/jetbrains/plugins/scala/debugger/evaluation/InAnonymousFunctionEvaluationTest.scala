package org.jetbrains.plugins.scala
package debugger
package evaluation

import org.junit.experimental.categories.Category

@Category(Array(classOf[DebuggerTests]))
class InAnonymousFunctionEvaluationTest_2_11 extends InAnonymousFunctionEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_11
}

@Category(Array(classOf[DebuggerTests]))
class InAnonymousFunctionEvaluationTest_2_12 extends InAnonymousFunctionEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_12

  override def testPartialFunction(): Unit = {
    createLocalProcess("PartialFunction")

    doWhenPausedThenResume { implicit context =>
      assertEquals("a", "a".evaluateAsString)
      assertEquals("x", "x".evaluateAsString)
      assertEquals("param", "param".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
      assertEquals("notUsed", "notUsed".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
    }
  }
}

@Category(Array(classOf[DebuggerTests]))
class InAnonymousFunctionEvaluationTest_2_13 extends InAnonymousFunctionEvaluationTest_2_12 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_13
}

@Category(Array(classOf[DebuggerTests]))
class InAnonymousFunctionEvaluationTest_3_0 extends InAnonymousFunctionEvaluationTest_2_13 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testFunctionExpression(): Unit = {}
}

@Category(Array(classOf[DebuggerTests]))
class InAnonymousFunctionEvaluationTest_3_1 extends InAnonymousFunctionEvaluationTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class InAnonymousFunctionEvaluationTestBase extends NewScalaDebuggerTestCase {
  def testFunctionValue(): Unit = {
    createLocalProcess("FunctionValue")

    doWhenPausedThenResume { implicit context =>
      assertEquals("a", "a".evaluateAsString)
      assertEquals("b", "b".evaluateAsString)
      assertEquals("x", "x".evaluateAsString)
      assertEquals(10, "n".evaluateAsInt)
      assertEquals("[]", "args".evaluateAsString)
    }
  }

  def testPartialFunction(): Unit = {
    createLocalProcess("PartialFunction")

    doWhenPausedThenResume { implicit context =>
      assertEquals("a", "a".evaluateAsString)
      assertEquals("10", "i".evaluateAsString)
      assertEquals("x", "x".evaluateAsString)
      assertEquals("param", "param".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
      assertEquals("notUsed", "notUsed".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
    }
  }

  def testFunctionExpression(): Unit = {
    createLocalProcess("FunctionExpression")

    doWhenPausedThenResume { implicit context =>
      assertEquals("a", "a".evaluateAsString)
      assertEquals("x", "x".evaluateAsString)
      assertEquals("param", "param".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
      assertEquals("notUsed", "notUsed".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
    }
  }

  def testForStatement(): Unit = {
    createLocalProcess("ForStatement")

    doWhenPausedThenResume { implicit context =>
      assertEquals("a", "s".evaluateAsString)
      assertEquals("in", "in".evaluateAsString)
      assertEquals("param", "param".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
      assertEquals("notUsed", "notUsed".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
      assertEquals("aa", "ss".evaluateAsString)
      assertEquals(ScalaBundle.message("not.used.from.for.statement", "i"), interceptEvaluationException("i"))
      assertEquals(ScalaBundle.message("not.used.from.for.statement", "si"), interceptEvaluationException("si"))
    }
  }
}
