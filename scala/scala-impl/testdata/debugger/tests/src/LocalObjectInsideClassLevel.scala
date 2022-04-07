object LocalObjectInsideClassLevel {
  def main(args: Array[String]): Unit = {
    class Local {
      def foo(): Unit = {
        val x = 1
        var s = "a"
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
    new Local().foo()
  }
}
