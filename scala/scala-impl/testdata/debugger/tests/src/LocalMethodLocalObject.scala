object LocalMethodLocalObject {
  def main(args: Array[String]): Unit = {
    object x
    def foo(y: Int): Unit = {
      val local = x
      // Breakpoint!
      println(local)
    }
    foo(2)
  }
}
