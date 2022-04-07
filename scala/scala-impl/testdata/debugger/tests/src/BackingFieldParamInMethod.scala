class BackingFieldParamInMethod(backingField: Int) {
  // Breakpoint!
  def foo: Int = backingField
}
object BackingFieldParamInMethod {
  def main(args: Array[String]): Unit = {
    val a = new BackingFieldParamInMethod(1)
    a.foo
  }
}
