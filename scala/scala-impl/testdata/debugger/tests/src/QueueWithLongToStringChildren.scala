object QueueWithLongToStringChildren {
  def main(args: Array[String]): Unit = {
    val queue = scala.collection.immutable.Queue(
      new LongToString(0),
      new LongToString(1),
      new LongToString(2),
      new LongToString(3),
      new LongToString(4)
    )
    // Breakpoint!
    val a = 1
  }
}

class LongToString(idx: Int) {
  override def toString: String = {
    Thread.sleep(1000) // ######### EMULATE LONG TO STRING EVALUATION #########
    s"To string result $idx!"
  }
}
