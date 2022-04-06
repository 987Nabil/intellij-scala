object MatchInForStatement {
  val name = "name"
  def main(args: Array[String]): Unit = {
    for (s <- List("a", "b"); if s == "a"; ss = s + s; i <- List(1, 2); if i == 1; si = s + i) {
      val x = (List(1, 2), Some("z"), ss :: i :: Nil)
      x match {
        case all @ (q :: qs, some@Some(z), list@List(m, _)) =>
          // Breakpoint!
          println()
        case _ =>
      }
    }
  }
}
