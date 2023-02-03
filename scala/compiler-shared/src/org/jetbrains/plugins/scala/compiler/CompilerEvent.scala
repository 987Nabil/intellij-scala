package org.jetbrains.plugins.scala.compiler

import org.jetbrains.jps.incremental.scala.Client
import org.jetbrains.plugins.scala.compiler.CompilerEventType.CompilerEventType
import org.jetbrains.plugins.scala.util.CompilationId

import java.io.File

sealed trait CompilerEvent {

  def eventType: CompilerEventType

  def compilationId: CompilationId

  def compilationUnitId: Option[CompilationUnitId]
}

object CompilerEvent {

  // can be sent multiple times for different modules by jps compiler
  case class CompilationStarted(override val compilationId: CompilationId,
                                override val compilationUnitId: Option[CompilationUnitId])
    extends CompilerEvent {
    
    override def eventType: CompilerEventType = CompilerEventType.CompilationStarted
  }

  case class CompilationPhase(override val compilationId: CompilationId,
                              override val compilationUnitId: Option[CompilationUnitId],
                              phase: String)
    extends CompilerEvent {

    override def eventType: CompilerEventType = CompilerEventType.CompilationPhase
  }

  case class CompilationUnit(override val compilationId: CompilationId,
                             override val compilationUnitId: Option[CompilationUnitId],
                             path: String)
    extends CompilerEvent {

    override def eventType: CompilerEventType = CompilerEventType.CompilationUnit
  }

  case class MessageEmitted(override val compilationId: CompilationId,
                            override val compilationUnitId: Option[CompilationUnitId],
                            msg: Client.ClientMsg)
    extends CompilerEvent {

    override def eventType: CompilerEventType = CompilerEventType.MessageEmitted
  }

  case class ProgressEmitted(override val compilationId: CompilationId,
                             override val compilationUnitId: Option[CompilationUnitId],
                             progress: Double)
    extends CompilerEvent {

    override def eventType: CompilerEventType = CompilerEventType.ProgressEmitted
  }
  
  // can be sent multiple times for different modules by jps compiler
  case class CompilationFinished(override val compilationId: CompilationId,
                                 override val compilationUnitId: Option[CompilationUnitId],
                                 sources: Set[File])
    extends CompilerEvent {

    override def eventType: CompilerEventType = CompilerEventType.CompilationFinished
  }

  final val BuilderId = "compiler-event"
}
