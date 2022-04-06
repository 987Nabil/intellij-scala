object InnerOuterInheritedOuterFieldEtc {
  class Outer extends BaseClass {
    class HasOuterField {
      def capture = Outer.this
    }
    class Z extends HasOuterField {
      def goo = {
        // Breakpoint!
        println()
      }
    }

    def goo = {
      new Z {}.goo
    }
  }
  def main(args: Array[String]): Unit = {
    new Outer().goo
  }
}
