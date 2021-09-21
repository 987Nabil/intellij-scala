package org.jetbrains.plugins.scala.lang.parser.parsing.types

import org.jetbrains.plugins.scala.ScalaBundle
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.parsing.ParsingRule
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.parser.util.{InScala3, ParserUtils}

/**
 * [[MatchTypeSuffix]] ::= [[InfixType]] `match` [[TypeCaseClauses]]
 */
object MatchTypeSuffix extends ParsingRule {
  override def parse(implicit builder: ScalaPsiBuilder): Boolean = {
    builder.getTokenType match {
      case ScalaTokenTypes.tLBRACE =>
        builder.advanceLexer()
        builder.enableNewlines()
        ParserUtils.parseLoopUntilRBrace() {
          if (!TypeCaseClauses())
            builder.error(ScalaBundle.message("match.type.cases.expected"))
        }
        builder.restoreNewlinesState()
      case InScala3(ScalaTokenTypes.kCASE) =>

        builder.findPreviousIndent match {
          case Some(indentationWidth) =>
            builder.withIndentationWidth(indentationWidth) {
              TypeCaseClauses()
            }
          case None =>
            builder.error(ScalaBundle.message("expected.case.on.a.new.line"))
        }
      case _ => builder.error(ScalaBundle.message("match.type.cases.expected"))
    }
    true
  }
}
