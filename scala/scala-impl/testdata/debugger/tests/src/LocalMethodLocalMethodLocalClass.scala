object LocalMethodLocalMethodLocalClass {
  def main(args: Array[String]): Unit = {
    val x = 1
    var s = "a"
    def foo(y: Int): Unit = {
      def foo(y: Int): Unit = {
        class A {
          def foo(): Unit = {
            // Breakpoint!
            println()
            s + x
          }
        }
        new A().foo()
      }
      foo(y)
    }
    foo(2)
  }
}
