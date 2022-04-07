object LocalOuter {
  def main(args: Array[String]): Unit = {
    val x = 1
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
