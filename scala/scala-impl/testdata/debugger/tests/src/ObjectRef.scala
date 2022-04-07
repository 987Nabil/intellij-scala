object ObjectRef {
  def main(args: Array[String]): Unit = {
    var n = "abc"
    for (_ <- 1 to 1) {
      // Breakpoint!
      n = "def"
    }
  }
}
