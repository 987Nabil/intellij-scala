class ThisInvocationFromInnerClass extends BaseClass {
  trait Z {
    def foo: Unit = {
      // Breakpoint!
      println()
    }
  }
  def boo(args: Array[String]): Unit = {
    new Z {}.foo
  }
}
object ThisInvocationFromInnerClass {
  def main(args: Array[String]): Unit = {
    val sample = new ThisInvocationFromInnerClass
    sample.boo(args)
  }
}
