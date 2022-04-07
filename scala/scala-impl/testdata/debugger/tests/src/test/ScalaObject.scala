package test

object ScalaObject {
  private[this] val privateThisVal: Double = 1.0
  private val privateVal: Int = 2
  private[test] val packagePrivateVal: String = "3"
  val publicVal: Array[Int] = Array.empty

  lazy val lazyVal: String = "lazy"

  private[this] var privateThisVar: Double = 4.0
  private var privateVar: Int = 5
  private[test] var packagePrivateVar: String = "6"
  var publicVar: Array[Int] = Array.empty

  override def hashCode: Int = 1

  def main(args: Array[String]): Unit = {
    // Need to use all private variables to avoid compiler optimizations
    val myThis = ScalaObject.this
    // Breakpoint!
    println(privateThisVal)
    println(privateVal)
    println(privateThisVar)
    println(privateVar)
  }
}
