package org.jetbrains.plugins.scala.debugger.renderers

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.settings.NodeRendererSettings
import org.jetbrains.plugins.scala.DebuggerTests
import org.jetbrains.plugins.scala.util.runners.{MultipleScalaVersionsRunner, RunWithScalaVersions, TestScalaVersion}
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(classOf[MultipleScalaVersionsRunner])
@RunWithScalaVersions(Array(
//  TestScalaVersion.Scala_2_11,
//  TestScalaVersion.Scala_2_12,
  TestScalaVersion.Scala_2_13,
//  TestScalaVersion.Scala_3_0,
//  TestScalaVersion.Scala_3_1
))
@Category(Array(classOf[DebuggerTests]))
class ScalaClassRendererTest extends RendererTestBase {
  def testScalaObject(): Unit = {
    testClassRenderer("test.ScalaObject")("myThis", "test.ScalaObject$", "@1",
      Set(
        "privateThisVal = 1.0",
        "privateVal = 2",
        "packagePrivateVal = 3",
        "publicVal = {int[0]@uniqueID}[]",
        "lazyVal = lazy",
        "privateThisVar = 4.0",
        "privateVar = 5",
        "packagePrivateVar = 6",
        "publicVar = {int[0]@uniqueID}[]"
      ))
  }

  def testScalaClass(): Unit = {
    testClassRenderer("test.Main")("myThis", "test.ScalaClass", "@1",
      Set(
        "privateThisVal = 1.0",
        "privateVal = 2",
        "packagePrivateVal = 3",
        "publicVal = {int[0]@uniqueID}[]",
        "lazyVal = lazy",
        "privateThisVar = 4.0",
        "privateVar = 5",
        "packagePrivateVar = 6",
        "publicVar = {int[0]@uniqueID}[]",
        "usedConstructorParam = 20"
      ))
  }

  private val UNIQUE_ID = "uniqueID"

  private def testClassRenderer(mainClassName: String)(
      varName: String,
      className: String,
      afterTypeLabel: String,
      expectedChildrenLabels: Set[String]): Unit = {

    createLocalProcess(mainClassName)

    doWhenXSessionPausedThenResume { () =>
      implicit val context: SuspendContextImpl = getDebugProcess.getDebuggerContext.getSuspendContext
      val (label, childrenLabels) =
        renderLabelAndChildren(varName, Some(expectedChildrenLabels.size))

      val classRenderer = NodeRendererSettings.getInstance().getClassRenderer
      val typeName = classRenderer.renderTypeName(className)
      val expectedLabel = s"$varName = {$typeName@$UNIQUE_ID}$className$afterTypeLabel"

      assertEquals(expectedLabel, label)
      assertEquals(expectedChildrenLabels, childrenLabels.toSet)
    }
  }
}
