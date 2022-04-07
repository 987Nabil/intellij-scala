object LocalMethodLocalMethodLocalClassLocalMethod {
  def main(args: Array[String]): Unit = {
    val x = 1
    def foo(y: Int): Unit = {
      def foo(y: Int): Unit = {
        class A {
          def foo(): Unit = {
            class B {
              def foo(): Unit = {
                def goo(y: Int): Unit = {
                  // Breakpoint!
                  println()
                  x
                }
                goo(x + 1)
              }
            }
            new B().foo()
          }
        }
        new A().foo()
      }
      foo(y)
    }
    foo(2)
  }
}
