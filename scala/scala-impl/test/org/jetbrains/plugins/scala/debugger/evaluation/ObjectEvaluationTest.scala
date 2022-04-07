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
class ObjectEvaluationTest_2 extends ObjectEvaluationTestBase

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_3_0 extends ObjectEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testInnerClassObjectFromObject(): Unit = {}
}

@Category(Array(classOf[DebuggerTests]))
class ObjectEvaluationTest_3_1 extends ObjectEvaluationTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class ObjectEvaluationTestBase extends NewScalaDebuggerTestCase {
  def testEvaluateObjects(): Unit = {
    createLocalProcess("HelloWorld")

    doWhenPausedThenResume { implicit context =>
      assertStartsWith("Simple$", "Simple".evaluateAsString)
      assertStartsWith("qual.Simple$", "qual.Simple".evaluateAsString)
      assertStartsWith("scala.collection.immutable.List$", "collection.immutable.List".evaluateAsString)
      assertEquals("SimpleCaseClass", "qual.SimpleCaseClass".evaluateAsString)
      assertStartsWith("qual.StableInner$Inner$", "qual.StableInner.Inner".evaluateAsString)
      assertStartsWith("qual.ClassInner$Inner$", "val x = new qual.ClassInner(); x.Inner".evaluateAsString)
    }
  }

  def testInnerClassObjectFromObject(): Unit = {
    createLocalProcess("InnerClassObjectFromObject")

    doWhenPausedThenResume { implicit context =>
      assertStartsWith("InnerClassObjectFromObject$S$SS$G", "SS.G".evaluateAsString)
      assertStartsWith("InnerClassObjectFromObject$S$SS$S", "SS.S".evaluateAsString)
      assertStartsWith("InnerClassObjectFromObject$S$SS$S", "S".evaluateAsString)
      assertStartsWith("InnerClassObjectFromObject$S$SS$", "SS".evaluateAsString)
    }
  }
}
