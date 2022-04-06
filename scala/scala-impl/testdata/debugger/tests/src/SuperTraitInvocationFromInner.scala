class SuperTraitInvocationFromInner extends BaseTrait {
  trait Z {
    def foo = {
      // Breakpoint!
      println()
    }
  }
  def boo(args: Array[String]) = {
    new Z {}.foo
  }
}
object SuperTraitInvocationFromInner {
  def main(args: Array[String]) = {
    new SuperTraitInvocationFromInner().boo(args)
  }
}
