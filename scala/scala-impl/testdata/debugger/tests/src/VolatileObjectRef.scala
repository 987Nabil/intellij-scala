object VolatileObjectRef {
  def main(args: Array[String]): Unit = {
    @volatile var n = "abc"
    for (_ <- 1 to 1) {
      // Breakpoint!
      n = "def"
    }
  }
}
