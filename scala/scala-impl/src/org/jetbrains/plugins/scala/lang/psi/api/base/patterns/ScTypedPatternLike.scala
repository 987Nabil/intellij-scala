package org.jetbrains.plugins.scala.lang.psi.api.base.patterns

trait ScTypedPatternLike extends ScPattern {
  def typePattern: Option[ScTypePattern]
}
