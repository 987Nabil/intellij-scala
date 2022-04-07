object LocalObjectInside {
  def main(args: Array[String]): Unit = {
    val x = 1
    object X {
      def foo(y: Int): Unit = {
        // Breakpoint!
        println()
        x
      }
    }
    X.foo(2)
  }
}
