object Multilevel {
  val name = "name"
  def main(args: Array[String]): Unit = {
    List(None, Some(1 :: 2 :: Nil)) match {
      case List(none, some) =>
        some match {
          case Some(seq) =>
            seq match {
              case Seq(1, two) =>
                // Breakpoint!
                println()
              case _ =>
            }
          case _ =>
        }
      case _ =>
    }
  }
}
