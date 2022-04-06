trait IOI {
  def ioi = 2
}
trait E extends IOI {
  trait FF {
    def ioi = 1
  }

  trait F extends FF {
    def foo = {
      E.super.ioi
      // Breakpoint!
      println()
    }
  }
  def moo = {new F{}.foo}
}
object OuterSuperInnerTraitInvocation {
  def main(args: Array[String]): Unit = {
    new E {}.moo
  }
}
