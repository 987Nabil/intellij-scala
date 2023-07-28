package org.jetbrains.plugins.scala.annotator

import com.intellij.psi.{PsiDocumentManager, PsiElement, PsiFile}
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.plugins.scala.annotator.hints.AnnotatorHints
import org.jetbrains.plugins.scala.extensions.{IterableOnceExt, PsiElementExt, StringExt}
import org.jetbrains.plugins.scala.util.assertions.MatcherAssertions
import org.junit.Assert.fail

trait ScalaHighlightingTestLike extends MatcherAssertions {
  protected def getFixture: CodeInsightTestFixture

  //////////////////////////////////////////////////
  // Assertions START
  //////////////////////////////////////////////////

  protected def assertNoErrors(code: String): Unit =
    assertErrors(code, Nil: _*)

  protected def assertErrors(code: String, messages: Message*): Unit =
    assertErrorsText(code, messages.mkString("\n"))

  protected def assertErrorsWithHints(code: String, messages: Message*): Unit =
    assertErrorsWithHintsText(code, messages.mkString("\n"))

  protected def assertMessages(code: String, messages: Message*): Unit =
    assertMessagesText(code, messages.mkString("\n"))

  protected def assertNoMessages(code: String): Unit =
    assertMessages(code, Nil: _*)

  protected def assertErrorsText(code: String, messagesConcatenated: String): Unit = {
    val actualMessages = errorsFromScalaCode(code)
    assertMessagesTextImpl(messagesConcatenated, actualMessages)
  }

  protected def assertErrorsWithHintsText(code: String, messagesConcatenated: String): Unit = {
    val actualMessages = errorsWithHintsFromScalaCode(code)
    assertMessagesTextImpl(messagesConcatenated, actualMessages)
  }

  protected def assertMessagesText(code: String, messagesConcatenated: String): Unit = {
    val actualMessages = messagesFromScalaCode(code)
    assertMessagesTextImpl(messagesConcatenated, actualMessages)
  }

  private def assertMessagesTextImpl(
    expectedMessagesConcatenated: String,
    actualMessages: Seq[Message],
  ): Unit = {
    // handle windows '\r', ignore empty lines
    val messagesConcatenatedClean =
      expectedMessagesConcatenated.withNormalizedSeparator.replaceAll("\\n\\n+", "\n").trim

    val actualMessagesConcatenated = actualMessages.mkString("\n")
    assertEqualsFailable(
      messagesConcatenatedClean,
      actualMessagesConcatenated
    )
  }

  //////////////////////////////////////////////////
  // Assertions END
  //////////////////////////////////////////////////

  //////////////////////////////////////////////////
  // Annotations extraction logic START
  //////////////////////////////////////////////////

  protected def errorsFromScalaCode(scalaFileText: String): List[Message.Error] =
    errorsFromScalaCode(scalaFileText, s"dummy.scala")

  protected def errorsWithHintsFromScalaCode(scalaFileText: String): List[Message] = {
    configureFile(scalaFileText, s"dummy.scala")
    errorsWithHintsFromScalaCode(getFixture.getFile)
  }

  protected def messagesFromScalaCode(scalaFileText: String): List[Message] = {
    configureFile(scalaFileText, s"dummy.scala")
    messagesFromScalaCode(getFixture.getFile)
  }

  protected def errorsFromScalaCode(scalaFileText: String, fileName: String): List[Message.Error] = {
    configureFile(scalaFileText, fileName)
    errorsFromScalaCode(getFixture.getFile)
  }

  private var filesCreated: Boolean = false

  private def configureFile(scalaFileText: String, fileName: String): Unit = {
    if (filesCreated)
      fail("Don't add files 2 times in a single test")

    getFixture.configureByText(fileName, scalaFileText)

    filesCreated = true
  }

  protected def errorsFromScalaCode(file: PsiFile): List[Message.Error] =
    nonEmptyMessagesFromScalaCode(file).filterByType[Message.Error]

  protected def errorsWithHintsFromScalaCode(file: PsiFile): List[Message] = {
    val errors = nonEmptyMessagesFromScalaCode(file).filterByType[Message.Error]

    val hints = file.elements
      .flatMap(AnnotatorHints.in(_).toSeq.flatMap(_.hints))
      .map(convertHintToTestHint)
      .toList

    hints ::: errors
  }

  private def convertHintToTestHint(hint: org.jetbrains.plugins.scala.annotator.hints.Hint): Message.Hint =
    Message.Hint(
      hint.element.getText,
      hint.parts.map(_.string).mkString,
      offsetDelta = hint.offsetDelta
    )

  private def nonEmptyMessagesFromScalaCode(file: PsiFile): List[Message] =
    messagesFromScalaCode(file).filter(m => m.element != null && m.message != null)

  private def messagesFromScalaCode(file: PsiFile): List[Message] = {
    PsiDocumentManager.getInstance(getFixture.getProject).commitAllDocuments()

    val annotationHolder: AnnotatorHolderMock = new AnnotatorHolderMock(file)

    file.depthFirst().foreach(annotate(_)(annotationHolder))

    annotationHolder.annotations
  }

  protected def annotate(element: PsiElement)
                        (implicit holder: ScalaAnnotationHolder): Unit =
    new ScalaAnnotator().annotate(element)

  //////////////////////////////////////////////////
  // Annotations extraction logic END
  //////////////////////////////////////////////////
}
