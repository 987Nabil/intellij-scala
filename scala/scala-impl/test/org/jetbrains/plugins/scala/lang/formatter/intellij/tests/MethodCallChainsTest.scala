package org.jetbrains.plugins.scala.lang.formatter.intellij.tests

import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import org.jetbrains.plugins.scala.ScalaLanguage
import org.jetbrains.plugins.scala.lang.formatter.AbstractScalaFormatterTestBase
import org.junit.Ignore


// Reminder about settings combinations:
// (vertical align / do not align)
// (no wrap / wrap if long / chop if long / wrap always )
// (wrap first / no wrap first) # only for "wrap always" and "chop if long"
class MethodCallChainsTest extends AbstractScalaFormatterTestBase {

  private val CHOP_DOWN_IF_LONG = CommonCodeStyleSettings.WRAP_ON_EVERY_ITEM
  private val RightMarginMarker = "!"

  private def setupRightMargin(rightMarginVisualHelper: String): Unit = {
    getSettings.setRightMargin(ScalaLanguage.INSTANCE, rightMarginVisualHelper.indexOf(RightMarginMarker))
  }

  //
  // Do not align when multiline
  //

  def test_MethodCallChain_DoNotWrap(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)"""
    doTextTest(before)
  }

  def test_MethodCallChain_DoNotWrap_1(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2)
        |.bar(3, 4)
        |.baz(5, 6).foo(7, 8)""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6).foo(7, 8)""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_WrapIfLong(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2).bar(3, 4)
        |  .baz(5, 6).foo(7, 8)
        |  .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_ChopDownIfLong(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6)
        |  .foo(7, 8)
        |  .bar(9, 10)
        |  .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_ChopDownIfLong_WrapFirstCall(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                               ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6)
        |  .foo(7, 8)
        |  .bar(9, 10)
        |  .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_WrapAlways(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |  .foo(1, 2)
        |  .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_WrapAlways_WrapFirstCall(): Unit = {
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .foo(1, 2)
        |  .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  //
  // Align when multiline
  //

  def test_MethodCallChain_Align_DoNotWrap(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2).bar(3, 4)
        |        .baz(5, 6).foo(7, 8)
        |        .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_FirstCallOnNewLine(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject
        |.foo(1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2).bar(3, 4)
        |  .baz(5, 6).foo(7, 8)
        |  .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_FirstCallOnNewLine_WithComment(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject // comment
        |.foo(1, 2)
        |.baz(5, 6)
        |
        |foo() // comment
        |.foo(1, 2)
        |.baz(3, 4)
        |
        |foo[String]() // comment
        |.foo(1, 2)
        |.baz(3, 4)
        |""".stripMargin
    val after =
      """myObject // comment
        |  .foo(1, 2)
        |  .baz(5, 6)
        |
        |foo() // comment
        |  .foo(1, 2)
        |  .baz(3, 4)
        |
        |foo[String]() // comment
        |  .foo(1, 2)
        |  .baz(3, 4)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_CommentsBetweenCalls(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.foo().bar()
        | //comment1
        |.foo().bar()
        | /*comment2*/
        |.foo().bar()
        | //comment4
        | /*comment3*/
        | /** comment4 */
        |.foo().bar()
        |
        |myObject/*comment0*/.foo().bar()
        | //comment1
        |.foo().bar()
        |
        |foo()/*comment0*/.foo().bar()
        | //comment1
        |.foo().bar()
        |
        |foo[Int]() /*comment0*/ .foo().bar()
        | //comment1
        |.foo().bar()
        |
        |myObject
        |/*comment0*/
        |.foo().bar()
        | //comment1
        |.foo().bar()
        |""".stripMargin
    val after =
      """myObject.foo().bar()
        |        //comment1
        |        .foo().bar()
        |        /*comment2*/
        |        .foo().bar()
        |        //comment4
        |        /*comment3*/
        |        /** comment4 */
        |        .foo().bar()
        |
        |myObject /*comment0*/ .foo().bar()
        |                      //comment1
        |                      .foo().bar()
        |
        |foo() /*comment0*/ .foo().bar()
        |                   //comment1
        |                   .foo().bar()
        |
        |foo[Int]() /*comment0*/ .foo().bar()
        |                        //comment1
        |                        .foo().bar()
        |
        |myObject
        |  /*comment0*/
        |  .foo().bar()
        |  //comment1
        |  .foo().bar()
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_CommentsAfterDot(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.foo().//comment1
        |bar()
        |./*comment2*/foo().bar()
        |""".stripMargin
    val after =
      """myObject.foo(). //comment1
        |        bar()
        |        . /*comment2*/ foo().bar()
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_CommentsAfterMethodName(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.foo().bar()
        |.foo/*comment2*/().bar()
        |""".stripMargin
    val after =
      """myObject.foo().bar()
        |        .foo /*comment2*/ ().bar()
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_CommentsMixed(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject/*comment1*/./*comment2*/foo/*comment3*/()""".stripMargin
    val after =
      """myObject /*comment1*/ . /*comment2*/ foo /*comment3*/ ()""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_MethodWithoutBrackets(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.foo.bar
        |.foo.bar()
        |.foo().bar
        |.foo.bar
        |""".stripMargin
    val after =
      """myObject.foo.bar
        |        .foo.bar()
        |        .foo().bar
        |        .foo.bar""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_WithTypeArguments(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """val myObject = myObject2.foo[String](0)
        |.bar[String](
        | 2
        |)
        |.baz
        |[String](
        |3
        |)
        |.kek
        |[String](4)
      """.stripMargin
    val after =
      """val myObject = myObject2.foo[String](0)
        |                        .bar[String](
        |                          2
        |                        )
        |                        .baz
        |                          [String](
        |                            3
        |                          )
        |                        .kek
        |                          [String](4)""".stripMargin
    doTextTest(before, after)
  }

  @Ignore("waiting for https://youtrack.jetbrains.com/issue/SCL-15163")
  def ignore_MethodCallChain_Align_DoNotWrap_FirstNewLineCallIsMultiline(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """val myObject = myObject2.foo(
        | 1
        |)
        |.bar(
        | 2
        |)
      """.stripMargin
    val after =
      """val myObject = myObject2.foo(
        |                          1
        |                        )
        |                        .bar(
        |                          2
        |                        )
      """.stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_DoNotWrap_FirstNewLineCallIsMultilineAndLast(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """val myObject = myObject2.foo(
        | 1
        |)
      """.stripMargin
    val after =
      """val myObject = myObject2.foo(
        |  1
        |)
      """.stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_WrapIfLong(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    setupRightMargin(
      """                              ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).looooooongMethod(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2).bar(3, 4)
        |        .baz(5, 6).foo(7, 8)
        |        .bar(9, 10)
        |        .looooooongMethod(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_ChopDownIfLong(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    setupRightMargin(
      """                                ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |        .bar(3, 4)
        |        .baz(5, 6)
        |        .foo(7, 8)
        |        .bar(9, 10)
        |        .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_ChopDownIfLong_WrapFirstCall(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CHOP_DOWN_IF_LONG
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                                ! """)
    val before =
      """myObject.foo(1, 2).bar(3, 4).baz(5, 6).foo(7, 8).bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .bar(3, 4)
        |  .baz(5, 6)
        |  .foo(7, 8)
        |  .bar(9, 10)
        |  .baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_WrapAlways(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject.foo(1, 2)
        |        .foo(1, 2)
        |        .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_Align_WrapAlways_WrapFirstCall(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    getCommonSettings.WRAP_FIRST_METHOD_IN_CALL_CHAIN = true
    setupRightMargin(
      """                                       ! """)
    val before =
      """myObject.foo(1, 2).foo(1, 2).foo(1, 2)
        |""".stripMargin
    val after =
      """myObject
        |  .foo(1, 2)
        |  .foo(1, 2)
        |  .foo(1, 2)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCall(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = false
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """foo[T1, T2](1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """foo[T1, T2](1, 2).bar(3, 4)
        |  .baz(5, 6).foo(7, 8)
        |  .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCall_Align(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """foo[T1, T2](1, 2).bar(3, 4)
        |.baz(5, 6).foo(7, 8)
        |.bar(9, 10).baz(11, 12)
        |""".stripMargin
    val after =
      """foo[T1, T2](1, 2).bar(3, 4)
        |                 .baz(5, 6).foo(7, 8)
        |                 .bar(9, 10).baz(11, 12)
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCallInTheMiddle_Align_1(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.method1[String]().method2()
        |.method3()""".stripMargin
    val after =
      """myObject.method1[String]().method2()
        |        .method3()
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCallInTheMiddle_Align_2(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.method1().method2[String]()
        |.method3()""".stripMargin
    val after =
      """myObject.method1().method2[String]()
        |        .method3()
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_GenericMethodCallInTheMiddle_Align_3(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    getCommonSettings.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.DO_NOT_WRAP
    setupRightMargin(
      """                  ! """)
    val before =
      """myObject.method[String]().method()
        |.method.method[T]()
        |.method[String]().method().method()
        |.method.method[T]()
        |.method""".stripMargin
    val after =
      """myObject.method[String]().method()
        |        .method.method[T]()
        |        .method[String]().method().method()
        |        .method.method[T]()
        |        .method
        |""".stripMargin
    doTextTest(before, after)
  }

  // this is some legacy peculiar case when first dot in method call chain starts in on previous line
  // still this does not work properly with wrapping styles different from NO_WRAP
  def test_MethodCallChain_Align_FirstDotOnPrevLine(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    val before =
      """val x = foo.
        |foo.goo.
        |foo(1, 2, 3).
        |foo.
        |foo
        |.foo
        |""".stripMargin
    val after =
      """val x = foo.
        |        foo.goo.
        |        foo(1, 2, 3).
        |        foo.
        |        foo
        |        .foo
        |""".stripMargin
    doTextTest(before, after)
  }

  def test_MethodCallChain_VariousKindsOfReceivers_Align(): Unit = {
    getCommonSettings.ALIGN_MULTILINE_CHAINED_METHODS = true
    doTextTest(
      """implicit class AnyOps(private val value: Any) extends AnyVal {
        |  def foo() = value
        |}
        |
        |val ref = 42
        |
        |ref.foo()
        |   .foo().foo()
        |   .foo().foo()
        |
        |
        |(ref).foo()
        |     .foo().foo()
        |     .foo().foo()
        |
        |(ref, ref).foo().foo()
        |          .foo()
        |
        |"12345".foo().foo()
        |       .foo()
        |
        |{
        |  42
        |}.foo().foo()
        | .foo().foo()
        |""".stripMargin
    )
  }


}
