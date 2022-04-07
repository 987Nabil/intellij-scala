class LocalClassParam(x: Int) {
  val h = x
  def foo(): Unit = {
    val y = () => {
      // Breakpoint!
      println()
      1 + 2 + x
    }
    y()
  }
}
object LocalClassParam {
  def main(args: Array[String]): Unit = {
    val a = new LocalClassParam(1)
    a.foo()
  }
}
