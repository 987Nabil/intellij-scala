package org.jetbrains.plugins.scala.lang.resolve.processors

/**
* @author Ilya Sergey
*
*/

import com.intellij.psi.scope._
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.scope.PsiScopeProcessor.Event

import org.jetbrains.plugins.scala.lang.psi.impl.top.defs._
import org.jetbrains.plugins.scala.lang.psi.impl.types._


class ClassObjectResolveProcessor(myName: String) extends ScalaClassResolveProcessor(myName) {

  override val canBeObject = true

  override def execute(element: PsiElement, substitutor: PsiSubstitutor): Boolean = {
    if (element.isInstanceOf[ScTmplDef]) {
      if (element.asInstanceOf[ScTmplDef].getName.equals(myName)) {
        myResult = element
        return false
      }
    }
    true
  }

}