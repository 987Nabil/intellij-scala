package org.jetbrains.plugins.scala.lang.dfa.controlFlow.transformations

import org.jetbrains.plugins.scala.lang.dfa.controlFlow.{ScalaDfaControlFlowBuilder, TransformationFailedException}
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScDeclaration, ScDefinitionWithAssignment}

/**
 * [[Transformable]] wrapper for all Scala PSI elements.
 *
 * It is the most likely entrypoint for building control flow for Scala code.
 * It passes responsibility further to more specific transformers.
 */
class ScalaPsiElementTransformer(val wrappedElement: ScalaPsiElement) extends Transformable {

  override def toString: String = s"ScalaPsiElementTransformer: $wrappedElement"

  override def transform(builder: ScalaDfaControlFlowBuilder): Unit = {
    val transformer = wrappedElement match {
      case expression: ScExpression => new ExpressionTransformer(expression)
      case definition: ScDefinitionWithAssignment => new DefinitionTransformer(definition)
      case declarationStatement: ScDeclaration with ScBlockStatement => new UnknownCallTransformer(declarationStatement)
      case _ => throw TransformationFailedException(wrappedElement, "Unsupported PSI element.")
    }

    transformer.transform(builder)
    builder.finishElement(wrappedElement)
  }

  protected def transformPsiElement(element: ScalaPsiElement, builder: ScalaDfaControlFlowBuilder): Unit = {
    new ScalaPsiElementTransformer(element).transform(builder)
  }

  protected def transformIfPresent(container: Option[ScalaPsiElement], builder: ScalaDfaControlFlowBuilder): Unit = {
    container match {
      case Some(element) => transformPsiElement(element, builder)
      case _ => builder.pushUnknownValue()
    }
  }
}
