package org.jetbrains.plugins.scala
package debugger.evaluateExpression

import org.jetbrains.plugins.scala.debugger._
import org.junit.experimental.categories.Category

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_2_11 extends VariablesFromPatternsEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_11
}

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_2_12 extends VariablesFromPatternsEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_12
}

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_2_13 extends VariablesFromPatternsEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_13
}

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_3_0 extends VariablesFromPatternsEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0

  override def testAnonymousInMatch(): Unit = failing(super.testAnonymousInMatch())
}

@Category(Array(classOf[DebuggerTests]))
class VariablesFromPatternsEvaluationTest_3_1 extends VariablesFromPatternsEvaluationTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class VariablesFromPatternsEvaluationTestBase extends ScalaDebuggerTestCase {
  addFileWithBreakpoints("RegexMatch.scala",
    {
      val pattern = """"(-)?(\\d+)(\\.\\d*)?".r"""
      s"""
         |object RegexMatch {
         |  val name = "name"
         |  def main(args: Array[String]): Unit = {
         |    val Decimal = $pattern
         |    "-2.5" match {
         |      case number @ Decimal(sign, _, dec) =>
         |        println()$bp
         |      case _ =>
         |    }
         |  }
         |}
      """.stripMargin.trim()
    }

  )

  def testRegexMatch(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("number", "-2.5")
      evalEquals("sign", "-")
      evalEquals("dec", ".5")
      evalEquals("name", "name")
    }
  }

  addFileWithBreakpoints("Multilevel.scala",
    s"""
       |object Multilevel {
       |  val name = "name"
       |  def main(args: Array[String]): Unit = {
       |    List(None, Some(1 :: 2 :: Nil)) match {
       |      case List(none, some) =>
       |        some match {
       |          case Some(seq) =>
       |            seq match {
       |              case Seq(1, two) =>
       |                println()$bp
       |              case _ =>
       |            }
       |          case _ =>
       |        }
       |      case _ =>
       |    }
       |  }
       |}
      """.stripMargin.trim()
  )

  def testMultilevel(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("name", "name")
      evalEquals("args", "[]")
      evalEquals("none", "None")
      evalEquals("some", "Some(List(1, 2))")
      evalEquals("seq", "List(1, 2)")
      evalEquals("two", "2")
    }
  }

  addFileWithBreakpoints("AnonymousInMatch.scala",
    s"""
       |object AnonymousInMatch {
       |  val name = "name"
       |  def main(args: Array[String]): Unit = {
       |    Option("a") match {
       |      case None =>
       |      case some @ Some(a) =>
       |        List(10) foreach { i =>
       |          println()$bp
       |        }
       |    }
       |  }
       |}
      """.stripMargin.trim()
  )

  def testAnonymousInMatch(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("name", "name")
      evalEquals("args", "[]")
      evalEquals("some", "Some(a)")
      evalEquals("a", "a")
      evalEquals("i", "10")
    }
  }
}
