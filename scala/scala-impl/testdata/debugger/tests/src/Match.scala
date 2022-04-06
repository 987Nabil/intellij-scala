object Match {
  val name = "name"
  def main(args: Array[String]): Unit = {
    val x = (List(1, 2), Some("z"), None)
    x match {
      case all @ (list @ List(q, w), some @ Some(z), _) =>
        // Breakpoint!
        println()
      case _ =>
    }
  }
}
