object FunctionValue {
  def main(args: Array[String]): Unit = {
    val a = "a"
    var b = "b"
    val f: (Int) => Unit = n => {
      val x = "x"
      // Breakpoint!
      println()
    }
    f(10)
  }
}
