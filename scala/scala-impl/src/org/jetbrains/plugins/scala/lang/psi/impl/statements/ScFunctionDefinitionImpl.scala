package org.jetbrains.plugins.scala.lang.psi.impl.statements

import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi._
import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.scala.JavaArrayFactoryUtil.ScFunctionDefinitionFactory
import org.jetbrains.plugins.scala.extensions.{StubBasedExt, ifReadAllowed}
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.ScalaElementType.FUNCTION_DEFINITION
import org.jetbrains.plugins.scala.lang.psi.api.ScalaElementVisitor
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.statements._
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.lang.psi.impl.base.ScNamedBeginImpl
import org.jetbrains.plugins.scala.lang.psi.impl.statements.ScFunctionDefinitionImpl.{importantOrderFunction, isCalculatingFor, returnTypeInner}
import org.jetbrains.plugins.scala.lang.psi.stubs.ScFunctionStub
import org.jetbrains.plugins.scala.lang.psi.stubs.elements.ScFunctionElementType
import org.jetbrains.plugins.scala.lang.psi.types.ValueClassType.{ImplicitValueClass, ImplicitValueClassDumbMode}
import org.jetbrains.plugins.scala.lang.psi.types.result._
import org.jetbrains.plugins.scala.lang.psi.types.{ScLiteralType, api}
import org.jetbrains.plugins.scala.util.UnloadableThreadLocal

class ScFunctionDefinitionImpl[S <: ScFunctionDefinition](stub: ScFunctionStub[S],
                                                          nodeType: ScFunctionElementType[S],
                                                          node: ASTNode)
  extends ScFunctionImpl(stub, nodeType, node)
    with ScFunctionDefinition
    with ScNamedBeginImpl {

  override def getContainingClass: PsiClass =
    super.getContainingClass match {
      case containingClazz@ImplicitValueClass(c) if !this.isPrivate && !this.isProtected =>
        c.fakeCompanionModule.getOrElse(containingClazz)
      case containingClazz => containingClazz
    }

  /**
   * Note that this method is only called in non-Scala contexts. For Scala contexts super#name is used.
   *
   * The below represents the special case for public function definitions of implicit classes that extend AnyVal -- a
   * common approach for Scala 2 extension methods. Such functions, from the perspective of non-Scala JVM languages,
   * have `$extension` appended to their name. See https://docs.scala-lang.org/overviews/core/value-classes.html#extension-methods.
   */
  override def getName: String =
    if (this.isPrivate || this.isProtected) {
      super.getName
    } else {
      containingClass match {
        case ImplicitValueClassDumbMode(_) => super.getName + "$extension"
        case _ => super.getName
      }
    }

  override protected def shouldProcessParameters(lastParent: PsiElement): Boolean =
    super.shouldProcessParameters(lastParent) || body.contains(lastParent)

  override def toString: String = "ScFunctionDefinition: " + ifReadAllowed(name)("")

  //types of implicit definitions without explicit type should be computed in the right order
  override def returnType: TypeResult = {
    if (importantOrderFunction(this)) {
      val parent = getParent
      val isCalculating = isCalculatingFor(parent)

      if (isCalculating.value) returnTypeInner(this)
      else {
        isCalculating.value = true
        try {
          val children = parent.stubOrPsiChildren(FUNCTION_DEFINITION, ScFunctionDefinitionFactory).iterator

          while (children.hasNext) {
            val nextFun = children.next()
            if (importantOrderFunction(nextFun)) {
              ProgressManager.checkCanceled()
              val nextReturnType = returnTypeInner(nextFun)

              //stop at current function to avoid recursion
              //if we are currently computing type in some implicit function body below
              if (nextFun == this) {
                return nextReturnType
              }
            }
          }
          returnTypeInner(this)
        }
        finally {
          isCalculating.value = false
        }
      }
    } else returnTypeInner(this)
  }

  override def body: Option[ScExpression] = byPsiOrStub(findChild[ScExpression])(_.bodyExpression)

  override def hasAssign: Boolean = byStubOrPsi(_.hasAssign)(assignment.isDefined)

  override def getBody: FakePsiCodeBlock = body match {
    case Some(b) => new FakePsiCodeBlock(b) // Needed so that LineBreakpoint.canAddLineBreakpoint allows line breakpoints on one-line method definitions
    case None    => null
  }

  override protected def acceptScala(visitor: ScalaElementVisitor): Unit =
    visitor.visitFunctionDefinition(this)

  override protected def keywordTokenType: IElementType = ScalaTokenTypes.kDEF

  override def namedTag: Option[ScNamedElement] = declaredElements.headOption
}

private object ScFunctionDefinitionImpl {
  import org.jetbrains.plugins.scala.project.UserDataHolderExt

  private val calculatingBlockKey: Key[UnloadableThreadLocal[Boolean]] = Key.create("calculating.function.returns.block")

  private def isCalculatingFor(e: PsiElement): UnloadableThreadLocal[Boolean] = e.getOrUpdateUserData(
    calculatingBlockKey,
    new UnloadableThreadLocal(false)
  )

  private def importantOrderFunction(function: ScFunction): Boolean = function match {
    case funDef: ScFunctionDefinition => funDef.hasModifierProperty("implicit") && !funDef.hasExplicitType
    case _ => false
  }

  private def returnTypeInner(fun: ScFunctionDefinition): TypeResult = {
    import fun.projectContext

    fun.returnTypeElement match {
      case None if !fun.hasAssign => Right(api.Unit)
      case None =>
        fun.body match {
          case Some(b) => b.`type`().map(ScLiteralType.widenRecursive)
          case _       => Right(api.Unit)
        }
      case Some(rte: ScTypeElement) => rte.`type`()
    }
  }
}
