object RegexMatch {
  val name = "name"
  def main(args: Array[String]): Unit = {
    val Decimal = "(-)?(\\d+)(\\.\\d*)?".r
    "-2.5" match {
      case number @ Decimal(sign, _, dec) =>
        // Breakpoint!
        println()
      case _ =>
    }
  }
}
