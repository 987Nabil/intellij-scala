object Lazy {
  def main(args: Array[String]): Unit = {
    val list = Stream.from(1)
    val stream = Stream.from(1)
    // Breakpoint!
    val a = 1
    val b = 2
  }
}
