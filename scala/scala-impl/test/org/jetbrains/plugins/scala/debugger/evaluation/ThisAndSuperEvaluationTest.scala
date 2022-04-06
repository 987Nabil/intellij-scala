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
  TestScalaVersion.Scala_2_13,
  TestScalaVersion.Scala_3_0,
  TestScalaVersion.Scala_3_1
))
@Category(Array(classOf[DebuggerTests]))
class ThisAndSuperEvaluationTest extends ThisAndSuperEvaluationTestBase

abstract class ThisAndSuperEvaluationTestBase extends NewScalaDebuggerTestCase {

  def testTraitThis(): Unit = {
    createLocalProcess("TraitThis")

    doWhenPausedThenResume { implicit context =>
      assertStartsWith("TraitThis$$anon", "this".evaluateAsString)
    }
  }

  def testSuperInvocation(): Unit = {
    createLocalProcess("SuperInvocation")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "foo".evaluateAsInt)
    }
  }

  def testInvocationFromInner(): Unit = {
    createLocalProcess("InvocationFromInner")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "foo".evaluateAsInt)
    }
  }

  def testThisInvocationFromInner(): Unit = {
    createLocalProcess("ThisInvocationFromInner")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "ThisInvocationFromInner.this.foo".evaluateAsInt)
    }
  }

  def testThisInvocationFromInnerClass(): Unit = {
    createLocalProcess("ThisInvocationFromInnerClass")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "ThisInvocationFromInnerClass.this.foo".evaluateAsInt)
    }
  }
}
