object PartialFunction {
  val name = "name"
  def main(args: Array[String]): Unit = {
    def printName(param: String, notUsed: String): Unit = {
      List(("a", 10)).foreach {
        case (a, i: Int) =>
          val x = "x"
          println(a + param)
          // Breakpoint!
          println()
      }
    }
    printName("param", "notUsed")
  }
}
