class BackingFieldParamInConstructor(backingField: Int) {
  // Breakpoint!
  println("in constructor")

  def foo: Int = backingField
}
object BackingFieldParamInConstructor {
  def main(args: Array[String]): Unit = {
    val a = new BackingFieldParamInConstructor(1)
    a.foo
  }
}
