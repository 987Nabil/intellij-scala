class ClassParamInConstructor(unused: Int, used: Int, val field: Int) {
  // Breakpoint!
  println(s"in constructor $used")
}
object ClassParamInConstructor {
  def main(args: Array[String]): Unit = {
    val a = new ClassParamInConstructor(1, 2, 3)
  }
}
