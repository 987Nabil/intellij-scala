package org.jetbrains.plugins.scala
package debugger
package evaluation

import com.sun.jdi._
import org.jetbrains.plugins.scala.util.runners._
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
class ExpressionEvaluationTest extends ExpressionEvaluationTestBase

abstract class ExpressionEvaluationTestBase extends NewScalaDebuggerTestCase {

  def testUnaryPrefix(): Unit = {
    createLocalProcess("UnaryPrefix")

    doWhenPausedThenResume { implicit context =>
      assertEquals(false, "!u".evaluateAsBoolean)
      assertEquals(false, "!true".evaluateAsBoolean)
      assertEquals(true, "!false".evaluateAsBoolean)
    }
  }

  def testVariousExpressions(): Unit = {
    createLocalProcess("HelloWorld")

    doWhenPausedThenResume { implicit context =>
      assertEquals("(1,2,3)", "(1, 2, 3)".evaluateAsString)
      assertEquals(voidValue(), """if (true) "text"""".evaluateAs[VoidValue])
      assertEquals("text", """if (true) "text" else "next"""".evaluateAsString)
      assertEquals("next", """if (false) "text" else "next"""".evaluateAsString)
      assertEquals(2, "if (false) 1 else if (true) 2 else 3".evaluateAsInt)
      assertEquals(true, """"text" != null""".evaluateAsBoolean)
      assertStartsWith("java.lang.Object@", "new Object()".evaluateAsString)
      assertStartsWith("java.lang.Object@", "new AnyRef()".evaluateAsString)
      assertEquals("class 'Any' is abstract; cannot be instantiated", interceptEvaluationException("new Any()"))
      assertEquals("class 'AnyVal' is abstract; cannot be instantiated", interceptEvaluationException("new AnyVal()"))
      assertEquals("class 'Unit' is abstract; cannot be instantiated", interceptEvaluationException("new Unit()"))
      assertEquals("class 'Null' is abstract; cannot be instantiated", interceptEvaluationException("new Null()"))
      assertEquals("class 'Nothing' is abstract; cannot be instantiated", interceptEvaluationException("new Nothing()"))
      assertEquals("trait 'Singleton' is abstract; cannot be instantiated", interceptEvaluationException("new Singleton()"))
      assertEquals("abc", """new String("abc")""".evaluateAsString)
      assertEquals(0, "new StringBuilder().## * 0".evaluateAsInt)
      assertEquals(voidValue(), "()".evaluateAs[VoidValue])
    }
  }

  def testSmartBoxing(): Unit = {
    createLocalProcess("SmartBoxing")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "test(tup)".evaluateAsInt)
      assertEquals(1, "test((1, 2))".evaluateAsInt)
      assertEquals(1, "test(Tuple2(1, 2))".evaluateAsInt)
      assertEquals(2, "test2(tup)".evaluateAsInt)
      assertEquals(2, "test2((1, 2))".evaluateAsInt)
      assertEquals(2, "test2(Tuple2(1, 2))".evaluateAsInt)
      assertEquals("1", "foo(1)(2)".evaluateAsString)
      assertEquals(2, "(scala.collection.immutable.HashSet.empty + 1 + 2).size".evaluateAsInt)
    }
  }

  def testAssignment(): Unit = {
    createLocalProcess("Assignment")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "x(0)(0)".evaluateAsInt)
      assertEquals(2, "x(0)(0) = 2".evaluateAsInt)
      assertEquals(2, "x(0)(0)".evaluateAsInt)
      assertEquals(1, "z".evaluateAsInt)
      assertEquals(2, "z = 2".evaluateAsInt)
      assertEquals(2, "z".evaluateAsInt)
      assertEquals(0, "m".evaluateAsInt)
      assertEquals(voidValue(), "m = 2".evaluateAs[VoidValue])
      assertEquals(2, "m".evaluateAsInt)
      assertEquals(1, "y = 1".evaluateAsInt) //local vals may be reassigned in debugger
      assertEquals(1, "y".evaluateAsInt)
      assertEquals(10, "ints(0) = 10".evaluateAsInt)
      assertEquals(10, "ints(0)".evaluateAsInt)
      assertEquals("10", "boxedAny(0) = 10".evaluateAsString)
      assertEquals("10", "boxedAny(0)".evaluateAsString)
      assertEquals("10", "boxedInteger(0) = 10".evaluateAsString)
      assertEquals("10", "boxedInteger(0)".evaluateAsString)
      assertEquals(true, "(boxedValues(0) = new Value(19)) == new Value(19)".evaluateAsBoolean)
      assertEquals(true, "boxedValues(0) == new Value(19)".evaluateAsBoolean)
      assertEquals(true, "(boxedValues(0) = (((((((((new Value(20))))))))))) == new Value(20)".evaluateAsBoolean)
      assertEquals(true, """(boxedStrings(0) = new StringValue("19")) == new StringValue("19")""".evaluateAsBoolean)
      assertEquals(true, """boxedStrings(0) == new StringValue("19")""".evaluateAsBoolean)
    }
  }

  def testNamedThis(): Unit = {
    createLocalProcess("NamedThis")

    doWhenPausedThenResume { implicit context =>
      assertEquals(1, "innerX".evaluateAsInt)
      assertEquals(1, "Inner.this.x".evaluateAsInt)
    }
  }
}
