object UnaryPrefix {
  class U {
    def unary_!(): Boolean = false
  }
  def main(args: Array[String]): Unit = {
    val u = new U()
    // Breakpoint!
    println()
  }
}
