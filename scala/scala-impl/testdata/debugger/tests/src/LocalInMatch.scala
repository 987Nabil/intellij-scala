object LocalInMatch {
  val name = "name"
  def main(args: Array[String]): Unit = {
    Option("a") match {
      case None =>
      case some@Some(a) =>
        def foo(i: Int): Unit = {
          // Breakpoint!
          println()
        }

        foo(10)
    }
  }
}
