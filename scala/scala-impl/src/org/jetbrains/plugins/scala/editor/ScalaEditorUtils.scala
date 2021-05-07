package org.jetbrains.plugins.scala.editor

import com.intellij.openapi.editor.Document
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile}
import org.jetbrains.annotations.Nullable

private[editor] object ScalaEditorUtils {

  /**
   * If caret is in the end of the document, file.findElementAt returns null.<br>
   * In this case, this method returns leaf last element in the file if it's non empty.
   */
  @Nullable
  def findElementAtCaret_WithFixedEOF(file: PsiFile, document: Document, caretOffset: Int): PsiElement =
    findElementAtCaret_WithFixedEOF(file, document.getTextLength, caretOffset)

  @Nullable
  def findElementAtCaret_WithFixedEOF(file: PsiFile, documentLength: => Int, caretOffset: Int): PsiElement = {
    val elementAtCaret = file.findElementAt(caretOffset)
    if (elementAtCaret == null && documentLength == caretOffset)
      PsiTreeUtil.getDeepestLast(file)
    else
      elementAtCaret
  }
}
