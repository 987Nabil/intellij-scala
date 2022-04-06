package org.jetbrains.plugins.scala
package debugger
package evaluation

import org.jetbrains.plugins.scala.util.runners._
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(classOf[MultipleScalaVersionsRunner])
@RunWithScalaVersions(Array(
  TestScalaVersion.Scala_2_11,
  TestScalaVersion.Scala_2_12,
  TestScalaVersion.Scala_2_13
))
@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest extends VariablesFromPatternsEvaluationTestBase

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_3_0 extends VariablesFromPatternsEvaluationTestBase {
  override def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testAnonymousInMatch(): Unit = {}
}

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_3_1 extends VariablesFromPatternsEvaluationTest_3_0 {
  override def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class VariablesFromPatternsEvaluationTestBase extends NewScalaDebuggerTestCase {
  def testMatch(): Unit = {
    createLocalProcess("Match")

    doWhenPausedThenResume { implicit context =>
      assertEquals("(List(1, 2),Some(z),None)", "all".evaluateAsString)
      assertEquals("List(1, 2)", "list".evaluateAsString)
      assertEquals("(List(1, 2),Some(z),None)", "x".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
      assertEquals("1", "q".evaluateAsString)
      assertEquals("z", "z".evaluateAsString)
      assertEquals("Some(z)", "some".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
    }
  }

  def testMatchInForStatement(): Unit = {
    createLocalProcess("MatchInForStatement")

    doWhenPausedThenResume { implicit context =>
      assertEquals("(List(1, 2),Some(z),List(aa, 1))", "all".evaluateAsString)
      assertEquals("(List(1, 2),Some(z),List(aa, 1))", "x".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
      assertEquals("1", "q".evaluateAsString)
      assertEquals("List(2)", "qs".evaluateAsString)
      assertEquals("z", "z".evaluateAsString)
      assertEquals("List(aa, 1)", "list".evaluateAsString)
      assertEquals("aa", "m".evaluateAsString)
      assertEquals("Some(z)", "some".evaluateAsString)
      assertEquals("aa", "ss".evaluateAsString)
      assertEquals("1", "i".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
    }
  }

  def testRegexMatch(): Unit = {
    createLocalProcess("RegexMatch")

    doWhenPausedThenResume { implicit context =>
      assertEquals("-2.5", "number".evaluateAsString)
      assertEquals("-", "sign".evaluateAsString)
      assertEquals(".5", "dec".evaluateAsString)
      assertEquals("name", "name".evaluateAsString)
    }
  }

  def testMultilevel(): Unit = {
    createLocalProcess("Multilevel")

    doWhenPausedThenResume { implicit context =>
      assertEquals("name", "name".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
      assertEquals("None", "none".evaluateAsString)
      assertEquals("Some(List(1, 2))", "some".evaluateAsString)
      assertEquals("List(1, 2)", "seq".evaluateAsString)
      assertEquals("2", "two".evaluateAsString)
    }
  }

  def testLocalInMatch(): Unit = {
    createLocalProcess("LocalInMatch")

    doWhenPausedThenResume { implicit context =>
      assertEquals("name", "name".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
      assertEquals("Some(a)", "some".evaluateAsString)
      assertEquals("a", "a".evaluateAsString)
      assertEquals(10, "i".evaluateAsInt)
    }
  }

  def testAnonymousInMatch(): Unit = {
    createLocalProcess("AnonymousInMatch")

    doWhenPausedThenResume { implicit context =>
      assertEquals("name", "name".evaluateAsString)
      assertEquals("[]", "args".evaluateAsString)
      assertEquals("Some(a)", "some".evaluateAsString)
      assertEquals("a", "a".evaluateAsString)
      assertEquals(10, "i".evaluateAsInt)
    }
  }
}
