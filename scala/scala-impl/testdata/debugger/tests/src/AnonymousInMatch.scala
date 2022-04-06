object AnonymousInMatch {
  val name = "name"
  def main(args: Array[String]): Unit = {
    Option("a") match {
      case None =>
      case some @ Some(a) =>
        List(10) foreach { i =>
          // Breakpoint!
          println()
        }
    }
  }
}
