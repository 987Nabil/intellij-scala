package org.jetbrains.plugins.scala
package debugger
package evaluateExpression

import org.junit.experimental.categories.Category

@Category(Array(classOf[DebuggerTests]))
class ScalaThisAndSuperEvaluationTest_2_11 extends ScalaThisAndSuperEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_11
}

@Category(Array(classOf[DebuggerTests]))
class ScalaThisAndSuperEvaluationTest_2_12 extends ScalaThisAndSuperEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_12
}

@Category(Array(classOf[DebuggerTests]))
class ScalaThisAndSuperEvaluationTest_2_13 extends ScalaThisAndSuperEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_2_13
}

@Category(Array(classOf[DebuggerTests]))
class ScalaThisAndSuperEvaluationTest_3_0 extends ScalaThisAndSuperEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_0
}

@Category(Array(classOf[DebuggerTests]))
class ScalaThisAndSuperEvaluationTest_3_1 extends ScalaThisAndSuperEvaluationTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version == LatestScalaVersions.Scala_3_1
}

abstract class ScalaThisAndSuperEvaluationTestBase extends ScalaDebuggerTestCase {

  addFileWithBreakpoints("SuperInvocationFromInner.scala",
    s"""
       |object SuperInvocationFromInner extends BaseClass {
       |  trait Z {
       |    def foo = {
       |      println()$bp
       |    }
       |  }
       |  def main(args: Array[String]): Unit = {
       |    new Z {}.foo
       |  }
       |}
      """.stripMargin.trim()
  )
  def testSuperInvocationFromInner(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("SuperInvocationFromInner.super.foo", "1")
    }
  }

  addFileWithBreakpoints("SuperTraitInvocationFromInner.scala",
    s"""
       |class SuperTraitInvocationFromInner extends BaseTrait {
       |  trait Z {
       |    def foo = {
       |      println()$bp
       |    }
       |  }
       |  def boo(args: Array[String]) = {
       |    new Z {}.foo
       |  }
       |}
       |object SuperTraitInvocationFromInner {
       |  def main(args: Array[String]) = {
       |    new SuperTraitInvocationFromInner().boo(args)
       |  }
       |}
      """.stripMargin.trim()
  )
  def testSuperTraitInvocationFromInner(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("SuperTraitInvocationFromInner.super.foo", "1")
    }
  }

  addFileWithBreakpoints("SuperTraitInvocation.scala",
    s"""
       |object SuperTraitInvocation extends BaseTrait {
       |  def main(args: Array[String]): Unit = {
       |    println()$bp
       |  }
       |}
      """.stripMargin.trim()
  )
  def testSuperTraitInvocation(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("foo", "1")
    }
  }

  addFileWithBreakpoints("Sample.scala",
    s"""
       |trait IOI {
       |  def ioi = 2
       |}
       |trait E extends IOI {
       |  trait FF {
       |    def ioi = 1
       |  }
       |
       |  trait F extends FF {
       |    def foo = {
       |      E.super.ioi
       |      println()$bp
       |    }
       |  }
       |  def moo = {new F{}.foo}
       |}
       |object OuterSuperInnerTraitInvocation {
       |  def main(args: Array[String]): Unit = {
       |    new E {}.moo
       |  }
       |}
      """.stripMargin.trim()
  )
  def testOuterSuperInnerTraitInvocation(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("E.super.ioi", "2")
    }
  }

  addFileWithBreakpoints("InnerOuterEtc.scala",
    s"""
       |object InnerOuterEtc {
       |  class Outer extends BaseClass {
       |    trait Z {
       |      def goo = {
       |        println()$bp
       |      }
       |    }
       |
       |    def goo = {
       |      new Z {}.goo
       |    }
       |  }
       |  def main(args: Array[String]): Unit = {
       |    new Outer().goo
       |  }
       |}
      """.stripMargin.trim()
  )
  def testInnerOuterEtc(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("foo", "1")
    }
  }

  addFileWithBreakpoints("InnerOuterInheritedOuterFieldEtc.scala",
    s"""
       |object InnerOuterInheritedOuterFieldEtc {
       |  class Outer extends BaseClass {
       |    class HasOuterField {
       |      def capture = Outer.this
       |    }
       |    class Z extends HasOuterField {
       |      def goo = {
       |        println()$bp
       |      }
       |    }
       |
       |    def goo = {
       |      new Z {}.goo
       |    }
       |  }
       |  def main(args: Array[String]): Unit = {
       |    new Outer().goo
       |  }
       |}
      """.stripMargin.trim()
  )
  def testInnerOuterInheritedOuterFieldEtc(): Unit = {
    runDebugger() {
      waitForBreakpoint()
      evalEquals("foo", "1")
    }
  }
}