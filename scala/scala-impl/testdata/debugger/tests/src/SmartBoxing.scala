object SmartBoxing {
  def foo[T](x: T)(y: T): T = x
  def main(args: Array[String]): Unit = {
    val tup = (1, 2)
    // Breakpoint!
    println()
  }
  def test(tup: (Int, Int)): Int = tup._1
  def test2(tup: Tuple2[Int, Int]): Int = tup._2
}
