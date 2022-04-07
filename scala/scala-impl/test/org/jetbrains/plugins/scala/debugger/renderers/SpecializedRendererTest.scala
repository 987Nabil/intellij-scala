package org.jetbrains.plugins.scala
package debugger
package renderers

import com.intellij.debugger.engine.SuspendContextImpl
import org.jetbrains.plugins.scala.util.runners.{MultipleScalaVersionsRunner, RunWithScalaVersions, TestScalaVersion}
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(classOf[MultipleScalaVersionsRunner])
@RunWithScalaVersions(Array(
  TestScalaVersion.Scala_2_12,
  TestScalaVersion.Scala_2_13,
  TestScalaVersion.Scala_3_0,
  TestScalaVersion.Scala_3_1
))
@Category(Array(classOf[DebuggerTests]))
class SpecializedRendererTest extends RendererTestBase {
  def testSpecializedTuple(): Unit = {
    checkChildrenNames("x", List("_1", "_2"))
  }

  private def checkChildrenNames(varName: String, childrenNames: Seq[String]): Unit = {
    createLocalProcess("SpecializedTuple")

    doWhenXSessionPausedThenResume { () =>
      implicit val context: SuspendContextImpl = getDebugProcess.getDebuggerContext.getSuspendContext
      val (_, labels) = renderLabelAndChildren(varName, Some(childrenNames.length))
      val names = labels.flatMap(_.split(" = ").headOption)
      assertEquals(childrenNames.sorted, names.sorted)
    }
  }
}
