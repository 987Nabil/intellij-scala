class NoBackingFieldParam(noBackingField: Int) {
  // Breakpoint!
  def foo: Int = 5
}
object NoBackingFieldParam {
  def main(args: Array[String]): Unit = {
    val a = new NoBackingFieldParam(1)
    a.foo
  }
}
