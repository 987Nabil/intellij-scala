package org.jetbrains.plugins.scala
package editor
package enterHandler

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.{PsiElement, PsiFile, PsiWhiteSpace}
import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.settings.ScalaApplicationSettings

final class AutoBraceEnterHandler extends EnterHandlerDelegateAdapter {

  override def preprocessEnter(file: PsiFile, editor: Editor, caretOffsetRef: Ref[Integer], caretAdvance: Ref[Integer],
                               dataContext: DataContext, originalHandler: EditorActionHandler): Result = {
    if (!file.is[ScalaFile])
      return Result.Continue

    if (!ScalaApplicationSettings.getInstance.HANDLE_BLOCK_BRACES_INSERTION_AUTOMATICALLY)
      return Result.Continue

    // TODO: review the behaviour for all Scala contexts in case the setting is disabled
    if (useIndentationBasedSyntax(file))
      return Result.Continue

    val caretOffset = caretOffsetRef.get.intValue

    val element = file.findElementAt(caretOffset)
    if (element == null || !hasFirstNewlineAfterCaret(element, caretOffset)) {
      return Result.Continue
    }

    val shouldIntend = AutoBraceUtils.previousExpressionInIndentationContext(element)

    if (shouldIntend.nonEmpty) Result.DefaultSkipIndent
    else Result.Continue
  }

  private def hasFirstNewlineAfterCaret(element: PsiElement, caretOffset: Int): Boolean =
    element match {
      case ws: PsiWhiteSpace =>
        val charsBeforeCaret = caretOffset - ws.getTextOffset
        val indexOfFirstNewline = ws.getText.indexOf('\n')
        indexOfFirstNewline >= charsBeforeCaret
      case _ => false
    }
}
