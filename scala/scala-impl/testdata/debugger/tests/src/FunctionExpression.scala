object FunctionExpression {
  val name = "name"
  def main(args: Array[String]): Unit = {
    def printName(param: String, notUsed: String): Unit = {
      List("a").foreach {
        a =>
          val x = "x"
          println(a + param)
          // Breakpoint!
          println()
      }
    }
    printName("param", "notUsed")
  }
}
