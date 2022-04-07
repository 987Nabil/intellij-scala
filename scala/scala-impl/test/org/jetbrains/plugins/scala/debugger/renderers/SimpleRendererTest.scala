package org.jetbrains.plugins.scala.debugger.renderers

import com.intellij.debugger.engine.SuspendContextImpl
import org.jetbrains.plugins.scala.DebuggerTests
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
class SimpleRendererTest extends RendererTestBase {
  private def checkLabelRendering(variableToExpectedLabel: (String, String)*): Unit = {
    createLocalProcess("LiteralRendering")

    doWhenXSessionPausedThenResume { () =>
      implicit val context: SuspendContextImpl = getDebugProcess.getDebuggerContext.getSuspendContext
      for {
        (variable, expected) <- variableToExpectedLabel
      } {
        val (label, _) = renderLabelAndChildren(variable, None)
        assertEquals(expected, label)
      }
    }
  }

  def testLiteralRendering(): Unit = {
    checkLabelRendering(
      "x1" -> "x1 = 42",
      "x2" -> "x2 = null",
      "x3" -> "x3 = 42",
      "x4" -> "x4 = true",
    )
  }
}
