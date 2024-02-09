package org.jetbrains.plugins.scala.format

import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.format.InterpolatedStringFormatter.{formatContent, injectByValue}
import org.jetbrains.plugins.scala.lang.psi.api.base.ScInterpolatedStringLiteral
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlockExpr
import org.jetbrains.plugins.scala.lang.refactoring.ScalaNamesValidator.isIdentifier
import org.jetbrains.plugins.scala.util.MultilineStringUtil.MultilineQuotes

class InterpolatedStringFormatter(val kind: ScInterpolatedStringLiteral.Kind) extends StringFormatter {
  override def format(parts: Seq[StringPart]): String = {
    val toMultiline = parts.exists {
      case Text(s)      => s.contains("\n")
      case i: Injection => i.value == "\n"
      case _            => false
    }
    format(parts, toMultiline, enforceInterpolator = false)
  }

  /**
   * @param enforceInterpolator when true, interpolator will be used even if it's not required
   */
  def format(
    parts: Seq[StringPart],
    toMultiline: Boolean,
    enforceInterpolator: Boolean
  ): String = {
    val prefix =
      if (!kind.is[ScInterpolatedStringLiteral.Pattern]) {
        val injections = parts.filterByType[Injection]

        if (injections.forall(injectByValue) && !enforceInterpolator) ""
        else if (injections.exists(_.isFormattingRequired)) ScInterpolatedStringLiteral.Format.prefix
        else kind.prefix
      }
      else kind.prefix
    val content = formatContent(parts, prefix, toMultiline)
    val quote = if (toMultiline) MultilineQuotes else "\""
    s"$prefix$quote$content$quote"
  }
}

object InterpolatedStringFormatter {
  def apply(kind: ScInterpolatedStringLiteral.Kind): InterpolatedStringFormatter =
    new InterpolatedStringFormatter(kind)

  def formatContent(parts: Seq[StringPart], prefix: String, toMultiline: Boolean): String = {
    val strings = parts.collect {
      case text: Text                         =>
        ScalaStringUtils.escapePlainText(text.value, toMultiline, prefix)
      case textFormatted: SpecialFormatEscape =>
        val isFormat = prefix == ScInterpolatedStringLiteral.Format.prefix
        val content = if (isFormat) textFormatted.originalText else textFormatted.unescapedText
        ScalaStringUtils.escapePlainText(content, toMultiline, prefix)
      case it: Injection =>
        if (injectByValue(it))
          it.value
        else {
          val text = it.text
          val skipBraces = (it.expression.is[ScBlockExpr] || !it.isLiteral && it.isAlphanumericIdentifier) && noBraces(parts, it)
          val presentation = if (skipBraces) "$" + text else "${" + text + "}"
          if (it.isFormattingRequired) presentation + it.format
          else presentation
        }
    }
    // micro-optimization for single Text()
    strings.size match {
      case 0 => ""
      case 1 => strings.head
      case _ => strings.mkString
    }
  }

  private def injectByValue(it: Injection) = it.isLiteral && !it.isFormattingRequired

  // TODO: WTF naming
  private def noBraces(parts: Seq[StringPart], it: Injection): Boolean = {
    val ind = parts.indexOf(it)
    if (ind + 1 < parts.size) {
      parts(ind + 1) match {
        case Text(s) =>
          return s.isEmpty || !isIdentifier(it.text + s.charAt(0)) ||
            s.startsWith("`") || s.exists(_ == '$')
        case _ =>
      }
    }
    true
  }
}
