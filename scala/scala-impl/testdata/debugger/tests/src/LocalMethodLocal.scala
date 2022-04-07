object LocalMethodLocal {
  def main(args: Array[String]): Unit = {
    val x: Int = 1
    var s = "a"
    def foo(y: Int): Unit = {
      // Breakpoint!
      println()
      x
    }
    foo(2)
  }
}
