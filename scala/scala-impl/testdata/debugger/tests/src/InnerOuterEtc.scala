object InnerOuterEtc {
  class Outer extends BaseClass {
    trait Z {
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
