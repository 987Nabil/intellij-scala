package org.jetbrains.plugins.scala.conversion.ast

/**
  * Created by Kate Ustyuzhanina
  * on 10/26/15
  */
case class MethodConstruction(modifiers: IntermediateNode, name: IntermediateNode, typeParams: Seq[IntermediateNode],
                              params: Seq[IntermediateNode], body: Option[IntermediateNode],
                              retType: Option[IntermediateNode]) extends IntermediateNode


trait Constructor

case class ConstructorSimply(modifiers: IntermediateNode, typeParams: Seq[IntermediateNode],
                             params: Seq[IntermediateNode], body: Option[IntermediateNode]) extends IntermediateNode
case class PrimaryConstruction(params: Seq[IntermediateNode], superCall: IntermediateNode,
                               body: Option[Seq[IntermediateNode]],  modifiers: IntermediateNode)
  extends IntermediateNode with Constructor



