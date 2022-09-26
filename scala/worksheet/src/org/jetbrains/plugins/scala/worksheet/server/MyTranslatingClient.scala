package org.jetbrains.plugins.scala.worksheet.server

import com.intellij.compiler.CompilerMessageImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.BuildMessage.Kind
import org.jetbrains.jps.incremental.scala.Client.PosInfo
import org.jetbrains.jps.incremental.scala.{Client, DummyClient}
import org.jetbrains.plugins.scala.extensions.LoggerExt
import org.jetbrains.plugins.scala.util.ScalaPluginUtils
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetDefaultSourcePreprocessor
import org.jetbrains.plugins.scala.worksheet.server.MyTranslatingClient.Log
import org.jetbrains.plugins.scala.worksheet.server.RemoteServerConnector.CompilerInterface

import scala.collection.mutable

private class MyTranslatingClient(
  project: Project,
  worksheet: VirtualFile,
  consumer: CompilerInterface
) extends DummyClient {
  private val endMarker = WorksheetDefaultSourcePreprocessor.ServiceMarkers.END_GENERATED_MARKER

  private def testLog(text: String, e: Throwable): Unit =
    e.printStackTrace()

  override def progress(text: String, done: Option[Float]): Unit =
    consumer.progress(text, done)

  override def trace(exception: Throwable): Unit =
    consumer.trace(exception)

  override def internalDebug(text: String): Unit = {
    Log.debugSafe(text)
    if (ApplicationManager.getApplication.isInternal || ScalaPluginUtils.isRunningFromSources)
      progress(s"internal message: $text", None)
  }

  override def message(msg: Client.ClientMsg): Unit = {
    val Client.ClientMsg(kind, text, _, PosInfo(line, column, _), _) = msg
    val lines = (if (text == null) "" else text).split("\n")
    val linesLength = lines.length

    //usually last line of error message represents the code for which error is produced
    val lastLine = lines.last

    val columnOffset: Int =
      if (linesLength >= 2) {
        //example error text:
        //Not found: unresolved1
        //def get$$instance$$res0 = /* ###worksheet### generated $$end$$ */ 111 + unresolved1

        val endLineIdx = lastLine.indexOf(endMarker)
        if (endLineIdx != -1)
          endLineIdx + endMarker.length
        else 0
      }
      else 0

    val finalText =
      if (columnOffset == 0)
        text
      else {
        val buffer = new mutable.StringBuilder
        for (lineIndex <- 0 until (linesLength - 1)) {
          buffer.append(lines(lineIndex)).append("\n")
        }
        buffer.append(lastLine.substring(columnOffset))
        buffer.toString
      }

    val lineOffset = WorksheetDefaultSourcePreprocessor.LinesOffsetToFixErrorPositionInFile

    val line1 = line.map(_ - lineOffset).map(_.toInt).getOrElse(-1)
    val column1 = column.map(_ - columnOffset).map(_.toInt).getOrElse(-1)

    val category = toCompilerMessageCategory(kind)

    val message = new CompilerMessageImpl(project, category, finalText, worksheet, line1, column1, null)
    consumer.message(message)
  }

  private def toCompilerMessageCategory(kind: Kind): CompilerMessageCategory = {
    import BuildMessage.Kind._
    kind match {
      case INFO | JPS_INFO | OTHER        => CompilerMessageCategory.INFORMATION
      case ERROR | INTERNAL_BUILDER_ERROR => CompilerMessageCategory.ERROR
      case PROGRESS                       => CompilerMessageCategory.STATISTICS
      case WARNING                        => CompilerMessageCategory.WARNING
    }
  }

  override def worksheetOutput(text: String): Unit =
    consumer.worksheetOutput(text)
}

object MyTranslatingClient {

  private val Log = Logger.getInstance(this.getClass)
}