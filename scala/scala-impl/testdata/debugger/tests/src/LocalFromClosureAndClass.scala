object LocalFromClosureAndClass {
  def main(args: Array[String]): Unit = {
    val x = 1
    var y = "a"
    val runnable = new Runnable {
      def run(): Unit = {
        val foo = () => {
          val runnable = new Runnable {
            def run(): Unit = {
              x
              // Breakpoint!
              println()
            }
          }
          runnable.run()
        }
        foo()
      }
    }
    runnable.run()
  }
}
