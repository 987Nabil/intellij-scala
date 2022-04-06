object ThisInvocationFromInner extends BaseClass {
  trait Z {
    def foo: Unit = {
      // Breakpoint!
      println()
    }
  }
  def main(args: Array[String]): Unit = {
    new Z {}.foo
  }
}
