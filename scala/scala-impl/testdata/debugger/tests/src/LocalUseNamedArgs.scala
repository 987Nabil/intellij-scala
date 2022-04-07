object LocalUseNamedArgs {
  def main(args: Array[String]): Unit = {
    val j = 2

    def inner(z: Int = 1) = bar(i = z, j = j)
    def inner2() = bar(i = 1, j = j)

    // Breakpoint!
    println()
  }

  def bar(i: Int, j: Int): Int = i + j
}
