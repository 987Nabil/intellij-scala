package org.jetbrains.plugins.scala
package annotator
package element

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.scala.extensions.ElementText
import org.jetbrains.plugins.scala.lang.psi.api.base._
import org.jetbrains.plugins.scala.lang.psi.api.base.literals.{ScIntegerLiteral, ScLongLiteral}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScPrefixExpr
import org.jetbrains.plugins.scala.project._

object ScLiteralAnnotator extends ElementAnnotator[ScLiteral] {

  import quickfix.NumberLiteralQuickFix._

  private val StringLiteralSizeLimit = 65536
  private val StringCharactersCountLimit = StringLiteralSizeLimit / 4

  override def annotate(literal: ScLiteral,
                        holder: AnnotationHolder,
                        typeAware: Boolean): Unit = {
    implicit val implicitHolder: AnnotationHolder = holder
    implicit val containingFile: PsiFile = literal.getContainingFile

    literal match {
      case _: ScLongLiteral => checkIntegerLiteral(literal, isLong = true)
      case _: ScIntegerLiteral => checkIntegerLiteral(literal, isLong = false)
      case interpolatedStringLiteral: ScInterpolatedStringLiteral =>
        createStringIsTooLongAnnotation(interpolatedStringLiteral, interpolatedStringLiteral.getStringParts: _*)
      case ScLiteral(string) => createStringIsTooLongAnnotation(literal, string)
      case _ =>
    }
  }

  private def createStringIsTooLongAnnotation[L <: ScLiteral](literal: L, strings: String*)
                                                             (implicit holder: AnnotationHolder,
                                                              containingFile: PsiFile) =
    if (strings.exists(stringIsTooLong)) {
      holder.createErrorAnnotation(
        literal,
        ScalaBundle.message("string.literal.is.too.long")
      )
    }

  private def stringIsTooLong(string: String)
                             (implicit containingFile: PsiFile): Boolean = string.length match {
    case length if length >= StringLiteralSizeLimit => true
    case length if length >= StringCharactersCountLimit => utf8Size(string) >= StringLiteralSizeLimit
    case _ => false
  }

  private def utf8Size(string: String)
                      (implicit containingFile: PsiFile): Int = {
    val lineSeparator = Option(containingFile)
      .flatMap(file => Option(file.getVirtualFile))
      .flatMap(virtualFile => Option(virtualFile.getDetectedLineSeparator))
      .getOrElse(Option(System.lineSeparator).getOrElse("\n"))

    string.map {
      case '\n' => lineSeparator.length
      case '\r' => 0
      case character if character >= 0 && character <= '\u007F' => 1
      case character if character >= '\u0080' && character <= '\u07FF' => 2
      case character if character >= '\u0800' && character <= '\uFFFF' => 3
      case _ => 4
    }.sum
  }

  private def checkIntegerLiteral(literal: ScLiteral, isLong: Boolean) // TODO isLong smells
                                 (implicit holder: AnnotationHolder): Unit = {
    import ScalaLanguageLevel._
    val languageLevel = literal.scalaLanguageLevel

    val text = literal.getLastChild.getText
    val kind = IntegerKind(text)

    val maybeParent = literal.getParent match {
      case prefixExpr: ScPrefixExpr =>
        // only "-1234" is negative, "- 1234" should be considered as positive 1234
        prefixExpr.getChildren match {
          case Array(ElementText("-"), _) => Some(prefixExpr)
          case _ => None
        }
      case _ => None
    }

    kind match {
      case Oct if languageLevel.exists(_ >= Scala_2_11) =>
        createOctToHexAnnotation(
          literal,
          ScalaBundle.message("octal.literals.removed")
        )
        return
      case Oct if languageLevel.contains(Scala_2_10) =>
        createOctToHexAnnotation(
          literal,
          ScalaBundle.message("octal.literals.deprecated"),
          ProblemHighlightType.LIKE_DEPRECATED
        )
      case _ =>
    }

    val number = kind(text, isLong)
    number.lastIndexOf('_') match {
      case -1 =>
      case index =>
        if (languageLevel.exists(_ < Scala_2_13)) {
          holder.createErrorAnnotation(literal, ScalaBundle.message("illegal.underscore.separator"))
        }
        if (index == number.length - 1) {
          holder.createErrorAnnotation(literal, ScalaBundle.message("trailing.underscore.separator"))
        }
    }

    parseIntegerNumber(number, kind, maybeParent.isDefined) match {
      case None =>
        holder.createErrorAnnotation(
          literal,
          ScalaBundle.message("long.literal.is.out.of.range")
        )
      case Some(Right(_)) if !isLong =>
        createToLongAnnotation(
          literal,
          ScalaBundle.message("integer.literal.is.out.of.range"),
          maybeParent
        )
      case _ =>
    }
  }

  private def createOctToHexAnnotation(literal: ScLiteral, message: String,
                                       highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR)
                                      (implicit holder: AnnotationHolder): Unit = {
    val annotation = highlightType match {
      case ProblemHighlightType.GENERIC_ERROR => holder.createErrorAnnotation(literal, message)
      case _ => holder.createWarningAnnotation(literal, message)
    }
    annotation.setHighlightType(highlightType)
    annotation.registerFix(new ConvertOctToHex(literal))
  }

  private def createToLongAnnotation(literal: ScLiteral, message: String,
                                     maybeParent: Option[ScPrefixExpr])
                                    (implicit holder: AnnotationHolder): Unit = {
    val expression = maybeParent.getOrElse(literal)
    val annotation = holder.createErrorAnnotation(expression, message)

    val shouldRegisterFix = expression.expectedType().forall {
      ConvertToLong.isApplicableTo(literal, _)
    }

    if (shouldRegisterFix) {
      annotation.registerFix(new ConvertToLong(literal))
    }
  }

  private[this] def parseIntegerNumber(number: String,
                                       kind: IntegerKind,
                                       isNegative: Boolean) =
    stringToNumber(number, kind, isNegative)().map {
      case (value, false) => Left(value.toInt)
      case (value, _) => Right(value)
    }

  @annotation.tailrec
  private[this] def stringToNumber(number: String,
                                   kind: IntegerKind,
                                   isNegative: Boolean)
                                  (index: Int = 0,
                                   value: Long = 0L,
                                   exceedsIntLimit: Boolean = false): Option[(Long, Boolean)] =
    if (index == number.length) {
      val newValue = if (isNegative) -value else value
      Some(newValue, exceedsIntLimit)
    } else {
      number(index) match {
        case '_' => stringToNumber(number, kind, isNegative)(index + 1, value, exceedsIntLimit)
        case char =>
          val digit = char.asDigit
          val IntegerKind(radix, divider) = kind
          val newValue = value * radix + digit

          def exceedsLimit(limit: Long) =
            limit / (radix / divider) < value ||
              limit - (digit / divider) < value * (radix / divider) &&
                !(isNegative && limit == newValue - 1)

          if (value < 0 || exceedsLimit(java.lang.Long.MAX_VALUE)) None
          else stringToNumber(number, kind, isNegative)(
            index + 1,
            newValue,
            value > Integer.MAX_VALUE || exceedsLimit(Integer.MAX_VALUE)
          )
      }
    }
}
