object InnerClassObjectFromObject {
  class S {
    object SS {
      object S {
        def foo(): Unit = {
          val local = SS.S //to have $$outer field
          // Breakpoint!
          println(local)
        }
      }
      object G
    }
    def foo(): Unit = {
      SS.S.foo()
    }
  }

  def main(args: Array[String]): Unit = {
    val x = new S()
    x.foo()
  }
}
