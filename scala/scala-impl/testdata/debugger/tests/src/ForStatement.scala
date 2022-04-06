object ForStatement {
  val name = "name"
  def main(args: Array[String]): Unit = {
    def printName(param: String, notUsed: String): Unit = {
      for (s <- List("a", "b"); if s == "a"; ss = s + s; i <- List(1,2); if i == 1; si = s + i) {
        val in = "in"
        println(s + param + ss)
        // Breakpoint!
        println()
      }
    }
    printName("param", "notUsed")
  }
}
