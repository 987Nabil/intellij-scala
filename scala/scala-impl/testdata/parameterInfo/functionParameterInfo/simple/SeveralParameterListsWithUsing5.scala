def foo(using a: Int)(b: Int)(implicit c: Int) = a

foo(<caret>)
//TEXT: (implicit a: Int)(b: Int)(implicit c: Int), STRIKEOUT: false