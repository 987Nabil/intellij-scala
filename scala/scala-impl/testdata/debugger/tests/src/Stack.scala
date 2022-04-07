object Stack {
  def main(args: Array[String]): Unit = {
    import scala.collection.mutable
    val stack = mutable.Stack(1, 2, 3, 4, 5, 6, 7, 8)
    // Breakpoint!
    val b = 45
  }
}
