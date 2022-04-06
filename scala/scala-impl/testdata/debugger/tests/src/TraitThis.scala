object TraitThis {
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
