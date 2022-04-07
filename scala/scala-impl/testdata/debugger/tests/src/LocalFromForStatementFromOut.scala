object LocalFromForStatementFromOut {
  def main(args: Array[String]): Unit = {
    val x = 1
    for (i <- 1 to 1) {
      x
      // Breakpoint!
      println()
    }
  }
}
