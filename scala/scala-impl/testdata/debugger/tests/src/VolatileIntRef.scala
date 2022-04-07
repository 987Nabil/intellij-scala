object VolatileIntRef {
  def main(args: Array[String]): Unit = {
    @volatile var n = 0
    for (_ <- 1 to 1) {
      // Breakpoint!
      n += 1
    }
  }
}
