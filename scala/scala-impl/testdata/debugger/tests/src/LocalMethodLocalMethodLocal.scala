object LocalMethodLocalMethodLocal {
  def main(args: Array[String]): Unit = {
    val x = 1
    var s = "a"
    def foo(y: Int): Unit = {
      def foo(y: Int): Unit = {
        // Breakpoint!
        println()
        x
      }
      foo(y)
    }
    foo(2)
  }
}
