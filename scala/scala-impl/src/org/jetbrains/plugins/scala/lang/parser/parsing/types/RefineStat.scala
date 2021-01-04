package org.jetbrains.plugins.scala
package lang
package parser
package parsing
package types

import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.parser.parsing.statements.{Dcl, Def, EmptyDcl}

/**
* @author Alexander Podkhalyuzin
* Date: 28.02.2008
*/

/*
 * RefineStat ::= Dcl
 *              | 'type' TypeDef
 */
object RefineStat extends ParsingRule {

  override def apply()(implicit builder: ScalaPsiBuilder): Boolean = {
    builder.getTokenType match {
      case ScalaTokenTypes.kTYPE =>
        if (!Def()) {
          if (!Dcl(isMod = false)) {
            EmptyDcl(isMod = false)
          }
        }
        true
      case ScalaTokenTypes.kVAR | ScalaTokenTypes.kVAL
           | ScalaTokenTypes.kDEF =>
        if (Dcl(isMod = false)) {
          true
        }
        else {
          EmptyDcl(isMod = false)
          true
        }
      case _ =>
        false
    }
  }
}