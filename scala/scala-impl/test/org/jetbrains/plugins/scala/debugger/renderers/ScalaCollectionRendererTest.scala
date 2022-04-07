package org.jetbrains.plugins.scala
package debugger
package renderers

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.settings.NodeRendererSettings
import com.intellij.debugger.ui.tree.render._
import org.junit.experimental.categories.Category

@Category(Array(classOf[DebuggerTests]))
class ScalaCollectionRendererTest_2_11 extends ScalaCollectionRendererTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_11
}
@Category(Array(classOf[DebuggerTests]))
class ScalaCollectionRendererTest_2_12 extends ScalaCollectionRendererTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_12

  def testLazy(): Unit = {
    testLazyCollectionRendering("Lazy", "stream", "scala.collection.immutable.Stream$Cons", "size = ?")
  }
}
@Category(Array(classOf[DebuggerTests]))
class ScalaCollectionRendererTest_2_13 extends ScalaCollectionRendererTest_2_12 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_13
}

@Category(Array(classOf[DebuggerTests]))
class ScalaCollectionRendererTest_3_0 extends ScalaCollectionRendererTest_2_13 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0
}

@Category(Array(classOf[DebuggerTests]))
class ScalaCollectionRendererTest_3_1 extends ScalaCollectionRendererTest_3_0 {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class ScalaCollectionRendererTestBase extends RendererTestBase {
  private val UNIQUE_ID = "uniqueID"

  protected def testCollectionRenderer(className: String,
                                       collectionName: String,
                                       collectionClass: String,
                                       afterTypeLabel: String,
                                       collectionLength: Option[Int]): Unit = {
    createLocalProcess(className)

    doWhenXSessionPausedThenResume { () =>
      implicit val context: SuspendContextImpl = getDebugProcess.getDebuggerContext.getSuspendContext
      val (label, children) = renderLabelAndChildren(collectionName, collectionLength)
      val classRenderer: ClassRenderer = NodeRendererSettings.getInstance().getClassRenderer
      val typeName = classRenderer.renderTypeName(collectionClass)
      val expectedLabel = s"$collectionName = {$typeName@$UNIQUE_ID}$afterTypeLabel"

      assertEquals(expectedLabel, label)

      if (collectionLength.isDefined) {
        val intType = classRenderer.renderTypeName("java.lang.Integer")
        val intLabel = s"{$intType@$UNIQUE_ID}"
        var testIndex = 0
        children.foreach { childLabel =>
          val expectedChildLabel = s"$testIndex = $intLabel${testIndex + 1}"

          try
            assertEquals(expectedChildLabel, childLabel)
          catch {
            case err: AssertionError =>
              val childrenDebugText = children.zipWithIndex
                .map { case (child, idx) => s"$idx: $child" }
                .mkString("\n")
              System.err.println(s"all children nodes labels:\n$childrenDebugText")
              throw err
          }
          testIndex += 1
        }
      }
    }
  }

  protected def testCollectionRenderer(className: String,
                                       collectionName: String,
                                       collectionClass: String,
                                       afterTypeLabel: String,
                                       expectedChildrenLabels: Seq[String]): Unit = {
    createLocalProcess(className)

    doWhenXSessionPausedThenResume { () =>
      implicit val context: SuspendContextImpl = getDebugProcess.getDebuggerContext.getSuspendContext
      val (label, childrenLabels) =
        renderLabelAndChildren(collectionName, Some(expectedChildrenLabels.length))

      val classRenderer: ClassRenderer = NodeRendererSettings.getInstance().getClassRenderer
      val typeName = classRenderer.renderTypeName(collectionClass)
      val expectedLabel = s"$collectionName = {$typeName@$UNIQUE_ID}$afterTypeLabel"

      assertEquals(expectedLabel, label)
      assertEquals(expectedChildrenLabels, childrenLabels)
    }
  }

  protected def testScalaCollectionRenderer(className: String, collectionName: String, collectionLength: Int, collectionClass: String): Unit = {
    val afterTypeLabel = s"size = $collectionLength"
    testCollectionRenderer(className, collectionName, collectionClass, afterTypeLabel, Some(collectionLength))
  }

  protected def testScalaCollectionRenderer(className: String,
                                            collectionName: String,
                                            collectionClass: String,
                                            expectedChildrenLabels: Seq[String]): Unit = {
    val collectionLength = expectedChildrenLabels.size
    val afterTypeLabel = s"size = $collectionLength"
    testCollectionRenderer(className, collectionName, collectionClass, afterTypeLabel, expectedChildrenLabels)
  }

  protected def testLazyCollectionRendering(className: String, collectionName: String, collectionClass: String, afterTypeLabel: String): Unit =
    testCollectionRenderer(className, collectionName, collectionClass, afterTypeLabel, None)

  def testShortList(): Unit = {
    testScalaCollectionRenderer("ShortList", "lst", 6, "scala.collection.immutable.$colon$colon")
  }

  def testStack(): Unit = {
    testScalaCollectionRenderer("Stack", "stack", 8, "scala.collection.mutable.Stack")
  }

  def testMutableList(): Unit = {
    testScalaCollectionRenderer("MutableList", "mutableList", 5, "scala.collection.mutable.ListBuffer")
  }

  def testQueue(): Unit = {
    testScalaCollectionRenderer("Queue", "queue", 4, "scala.collection.immutable.Queue")
  }

  def testQueueWithLongToStringChildren(): Unit = {
    val expectedChildrenLabels = Seq(
      s"""0 = {LongToString@$UNIQUE_ID}To string result 0!""",
      s"""1 = {LongToString@$UNIQUE_ID}To string result 1!""",
      s"""2 = {LongToString@$UNIQUE_ID}To string result 2!""",
      s"""3 = {LongToString@$UNIQUE_ID}To string result 3!""",
      s"""4 = {LongToString@$UNIQUE_ID}To string result 4!""",
    )
    testScalaCollectionRenderer("QueueWithLongToStringChildren", "queue", "scala.collection.immutable.Queue", expectedChildrenLabels)
  }

  def testLongList(): Unit = {
    testScalaCollectionRenderer("LongList", "longList", 50, "scala.collection.immutable.$colon$colon")
  }
}
