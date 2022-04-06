object NamedThis {
  def main(args: Array[String]): Unit = {
    class Inner {
      val x = 1
      def foo(): Unit = {
        val runnable = new Runnable() {
          def run: Unit = {
            val x = () => {
              val innerX = Inner.this.x
              // Breakpoint!
              println(innerX)
            }
            x()
          }
        }

        runnable.run()
      }
    }

    new Inner().foo()
  }
}
