object LocalParam {
  def main(args: Array[String]): Unit = {
    def foo(x: Int): Unit = {
      // Breakpoint!
      println()
    }
    foo(1)
  }
}
