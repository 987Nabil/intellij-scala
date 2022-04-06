object InvocationFromInner extends BaseClass {
  trait Z {
    def goo: Unit = {
      // Breakpoint!
      println()
    }
  }
  def main(args: Array[String]): Unit = {
    new Z {}.goo
  }
}
