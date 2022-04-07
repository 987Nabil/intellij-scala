object LocalObjectOuter {
  def main(args: Array[String]): Unit = {
    object x {}
    val runnable = new Runnable {
      def run(): Unit = {
        val runnable = new Runnable {
          def run(): Unit = {
            x
            // Breakpoint!
            println()
          }
        }
        runnable.run()
      }
    }
    runnable.run()
  }
}