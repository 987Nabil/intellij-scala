object Param {
  def foo(x: Int): Unit = {
    // Breakpoint!
    println()
  }

  def main(args: Array[String]): Unit = {
    val x = 0
    foo(x + 1)
  }
}
