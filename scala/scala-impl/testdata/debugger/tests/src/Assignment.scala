object Assignment {
  class Value(val n: Int) extends AnyVal

  class StringValue(val s: String) extends AnyVal

  var m = 0
  def main(args: Array[String]): Unit = {
    var z = 1
    val y = 0
    val x: Array[Array[Int]] = Array(Array(1, 2), Array(2, 3))
    val ints: Array[Int] = Array(1, 2)

    val boxedAny = Array[Any](1, 2)
    val boxedInteger = Array[java.lang.Integer](1, 2)

    val boxedValues = Array(new Value(1), new Value(2))
    val boxedStrings = Array(new StringValue("1"), new StringValue("2"))
    // Breakpoint!
    println()
  }
}
