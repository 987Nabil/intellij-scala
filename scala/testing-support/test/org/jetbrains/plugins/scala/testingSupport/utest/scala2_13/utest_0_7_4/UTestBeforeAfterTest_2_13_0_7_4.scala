package org.jetbrains.plugins.scala.testingSupport.utest.scala2_13.utest_0_7_4

class UTestBeforeAfterTest_2_13_0_7_4 extends UTestTestBase_2_13_0_7_4 {

  val beforeAfterTestName = "BeforeAfterTest"
  val beforeAfterFileName = beforeAfterTestName + ".scala"

  addSourceFile(beforeAfterFileName,
    s"""
       |import utest._
       |
       |object $beforeAfterTestName extends TestSuite {
       |  val tests = Tests {
       |    test("test1") {}
       |  }
       |
       |  override def utestBeforeEach(path: Seq[String]): Unit = { println("$TestOutputPrefix BEFORE $TestOutputSuffix") }
       |
       |  override def utestAfterEach(path: Seq[String]): Unit = { println("$TestOutputPrefix AFTER $TestOutputSuffix") }
       |}
       |""".stripMargin.trim())

  def testBefore(): Unit =
    runTestByLocation(
      loc(beforeAfterFileName, 4, 10),
      assertConfigAndSettings(_, beforeAfterTestName, "tests\\test1"),
      IgnoreTreeResult,
      AssertTestOutputTextContains("BEFORE")
    )

  def testAfter(): Unit =
    runTestByLocation(
      loc(beforeAfterFileName, 4, 10),
      assertConfigAndSettings(_, beforeAfterTestName, "tests\\test1"),
      IgnoreTreeResult,
      AssertTestOutputTextContains("AFTER")
    )
}
