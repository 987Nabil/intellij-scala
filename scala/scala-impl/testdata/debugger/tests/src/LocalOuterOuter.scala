object LocalOuterOuter {
  def main(args: Array[String]): Unit = {
    val x = 1
    var y = "a"
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
