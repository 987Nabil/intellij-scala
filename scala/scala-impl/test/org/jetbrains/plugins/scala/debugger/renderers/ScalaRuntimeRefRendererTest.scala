package org.jetbrains.plugins.scala
package debugger.renderers

import com.intellij.debugger.engine.SuspendContextImpl
import org.jetbrains.plugins.scala.util.runners.{MultipleScalaVersionsRunner, RunWithScalaVersions, TestScalaVersion}
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(classOf[MultipleScalaVersionsRunner])
@RunWithScalaVersions(Array(
//  TestScalaVersion.Scala_2_12,
  TestScalaVersion.Scala_2_13,
//  TestScalaVersion.Scala_3_0,
//  TestScalaVersion.Scala_3_1
))
@Category(Array(classOf[DebuggerTests]))
class ScalaRuntimeRefRendererTest extends RendererTestBase {
  def testIntRef(): Unit = {
    testRuntimeRef("IntRef", "n", "Int", "0")
  }

  def testVolatileIntRef(): Unit = {
    testRuntimeRef("VolatileIntRef", "n", "volatile Int", "0")
  }

  def testObjectRef(): Unit = {
    testRuntimeRef("ObjectRef", "n", "Object", """"abc"""")
  }

  def testVolatileObjectRef(): Unit = {
    testRuntimeRef("VolatileObjectRef", "n", "volatile Object", """"abc"""")
  }

  private def testRuntimeRef(className: String, varName: String, refType: String, afterTypeLabel: String): Unit = {
    createLocalProcess(className)


    doWhenXSessionPausedThenResume { () =>
      implicit val context: SuspendContextImpl = getDebugProcess.getDebuggerContext.getSuspendContext
      val (label, _) = renderLabelAndChildren(varName, None, parameter(0))
      val expectedLabel = s"{unwrapped Scala runtime $refType reference}$afterTypeLabel"
      if (!label.contains(expectedLabel)) {
        org.junit.Assert.fail("BLAH!!!!")
      }
    }
  }
}
