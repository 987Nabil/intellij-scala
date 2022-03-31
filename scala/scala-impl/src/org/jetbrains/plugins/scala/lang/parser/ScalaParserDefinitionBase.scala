package org.jetbrains.plugins.scala
package lang
package parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.{ASTNode, ParserDefinition}
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.{FileViewProvider, PsiElement}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.impl.base.literals._
import org.jetbrains.plugins.scala.lang.psi.impl.base.patterns._
import org.jetbrains.plugins.scala.lang.psi.impl.base.types._
import org.jetbrains.plugins.scala.lang.psi.impl.base._
import org.jetbrains.plugins.scala.lang.psi.impl.expr.xml._
import org.jetbrains.plugins.scala.lang.psi.impl.expr._
import org.jetbrains.plugins.scala.lang.psi.impl.statements.params.ScParameterTypeImpl
import org.jetbrains.plugins.scala.lang.scaladoc.psi.impl.ScDocResolvableCodeReferenceImpl

//noinspection TypeAnnotation
abstract class ScalaParserDefinitionBase protected() extends ParserDefinition {

  override def createLexer(project: Project) =
    new lexer.ScalaLexer(false, project)

  override def createParser(project: Project) = new ScalaParser(false)

  override def createElement(node: ASTNode): PsiElement = node.getElementType match {
    case creator: SelfPsiCreator => creator.createElement(node) // stub elements still implement this trait
    case elt =>
      import ScalaElementType._

      if (elt == ScCodeBlockElementType.BlockExpression)
        PsiUtilCore.NULL_PSI_ELEMENT

      /* Definition Parts */
      else if (elt == CONSTRUCTOR)
        new ScConstructorInvocationImpl(node)
      else if (elt == PARAM_TYPE)
        new ScParameterTypeImpl(node)
      else if (elt == SEQUENCE_ARG)
        new ScSequenceArgImpl(node)
      else if (elt == REFERENCE)
        new ScStableCodeReferenceImpl(node)
      else if (elt == DOC_REFERENCE)
      /* NOTE: only created to be used from
       * [[org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory#createDocReferenceFromText]]
       * to create a syntetic reference from doc
       */
        new ScDocResolvableCodeReferenceImpl(node)
      else if (elt == NAME_VALUE_PAIR)
        new ScNameValuePairImpl(node)
      else if (elt == ANNOTATION_EXPR)
        new ScAnnotationExprImpl(node)
      else if (elt == END_STMT)
        new ScEndImpl(node)

      /* Types */
      else if (elt == COMPOUND_TYPE)
        new ScCompoundTypeElementImpl(node)
      else if (elt == EXISTENTIAL_TYPE)
        new ScExistentialTypeElementImpl(node)
      else if (elt == SIMPLE_TYPE)
        new ScSimpleTypeElementImpl(node)
      else if (elt == INFIX_TYPE)
        new ScInfixTypeElementImpl(node)
      else if (elt == TYPE)
        new ScFunctionalTypeElementImpl(node)
      else if (elt == ANNOT_TYPE)
        new ScAnnotTypeElementImpl(node)
      else if (elt == WILDCARD_TYPE)
        new ScWildcardTypeElementImpl(node)
      else if (elt == TUPLE_TYPE)
        new ScTupleTypeElementImpl(node)
      else if (elt == TYPE_IN_PARENTHESIS)
        new ScParenthesisedTypeElementImpl(node)
      else if (elt == TYPE_PROJECTION)
        new ScTypeProjectionImpl(node)
      else if (elt == TYPE_GENERIC_CALL)
        new ScParameterizedTypeElementImpl(node)
      else if (elt == LITERAL_TYPE)
        new ScLiteralTypeElementImpl(node)
      else if (elt == TYPE_VARIABLE)
        new ScTypeVariableTypeElementImpl(node)
      else if (elt == SPLICED_BLOCK_TYPE)
        new ScSplicedBlockImpl(node)
      else if (elt == TYPE_LAMBDA)
        new ScTypeLambdaTypeElementImpl(node)
      else if (elt == MATCH_TYPE)
        new ScMatchTypeElementImpl(node)
      else if (elt == POLY_FUNCTION_TYPE)
        new ScPolyFunctionTypeElementImpl(node)
      else if (elt == DEPENDENT_FUNCTION_TYPE)
        new ScDependentFunctionTypeElementImpl(node)

      /* Type parts */
      else if (elt == TYPE_ARGS)
        new ScTypeArgsImpl(node)
      else if (elt == EXISTENTIAL_CLAUSE)
        new ScExistentialClauseImpl(node)
      else if (elt == TYPES)
        new ScTypesImpl(node)
      else if (elt == TYPE_CASE_CLAUSES)
        new ScMatchTypeCasesImpl(node)
      else if (elt == TYPE_CASE_CLAUSE)
        new ScMatchTypeCaseImpl(node)

      /* Expressions */
      else if (elt == PREFIX_EXPR)
        new ScPrefixExprImpl(node)
      else if (elt == POSTFIX_EXPR)
        new ScPostfixExprImpl(node)
      else if (elt == INFIX_EXPR)
        new ScInfixExprImpl(node)
      else if (elt == PLACEHOLDER_EXPR)
        new ScUnderscoreSectionImpl(node)
      else if (elt == PARENT_EXPR)
        new ScParenthesisedExprImpl(node)
      else if (elt == METHOD_CALL)
        new ScMethodCallImpl(node)
      else if (elt == REFERENCE_EXPRESSION)
        new ScReferenceExpressionImpl(node)
      else if (elt == THIS_REFERENCE)
        new ScThisReferenceImpl(node)
      else if (elt == SUPER_REFERENCE)
        new ScSuperReferenceImpl(node)
      else if (elt == GENERIC_CALL)
        new ScGenericCallImpl(node)
      else if (elt == FUNCTION_EXPR)
        new ScFunctionExprImpl(node)
      else if (elt == POLY_FUNCTION_EXPR)
        new ScPolyFunctionExprImpl(node)
      else if (elt == BLOCK)
        new ScBlockImpl(node)
      else if (elt == SPLICED_BLOCK_EXPR)
        new ScSplicedBlockImpl(node)
      else if (elt == QUOTED_BLOCK)
        new ScQuotedBlockImpl(node)
      else if (elt == QUOTED_TYPE)
        new ScQuotedTypeImpl(node)
      else if (elt == TUPLE)
        new ScTupleImpl(node)
      else if (elt == UNIT_EXPR)
        new ScUnitExprImpl(node)
      else if (elt == CONSTR_BLOCK_EXPR)
        new ScConstrBlockExprImpl(node)
      else if (elt == SELF_INVOCATION)
        new ScSelfInvocationImpl(node)
      else if (elt == NullLiteral)
        new ScNullLiteralImpl(node, NullLiteral.toString)
      else if (elt == LongLiteral)
        new ScLongLiteralImpl(node, LongLiteral.toString)
      else if (elt == IntegerLiteral)
        new ScIntegerLiteralImpl(node, IntegerLiteral.toString)
      else if (elt == DoubleLiteral)
        new ScDoubleLiteralImpl(node, DoubleLiteral.toString)
      else if (elt == FloatLiteral)
        new ScFloatLiteralImpl(node, FloatLiteral.toString)
      else if (elt == BooleanLiteral)
        new ScBooleanLiteralImpl(node, BooleanLiteral.toString)
      else if (elt == SymbolLiteral)
        new ScSymbolLiteralImpl(node, SymbolLiteral.toString)
      else if (elt == CharLiteral)
        new ScCharLiteralImpl(node, CharLiteral.toString)
      else if (elt == StringLiteral)
        new ScStringLiteralImpl(node, StringLiteral.toString)
      else if (elt == InterpolatedString)
        new ScInterpolatedStringLiteralImpl(node, InterpolatedString.toString)
      else if (elt == INTERPOLATED_PREFIX_LITERAL_REFERENCE)
        new ScInterpolatedExpressionPrefix(node)

      /* Composite expressions */

      else if (elt == IF_STMT)
        new ScIfImpl(node)
      else if (elt == FOR_STMT)
        new ScForImpl(node)
      else if (elt == DO_STMT)
        new ScDoImpl(node)
      else if (elt == TRY_STMT)
        new ScTryImpl(node)
      else if (elt == CATCH_BLOCK)
        new ScCatchBlockImpl(node)
      else if (elt == FINALLY_BLOCK)
        new ScFinallyBlockImpl(node)
      else if (elt == WHILE_STMT)
        new ScWhileImpl(node)
      else if (elt == RETURN_STMT)
        new ScReturnImpl(node)
      else if (elt == THROW_STMT)
        new ScThrowImpl(node)
      else if (elt == ASSIGN_STMT)
        new ScAssignmentImpl(node)
      else if (elt == MATCH_STMT)
        new ScMatchImpl(node)
      else if (elt == TYPED_EXPR_STMT)
        new ScTypedExpressionImpl(node)

      /* Expression Parts */
      else if (elt == GENERATOR)
        new ScGeneratorImpl(node)
      else if (elt == FOR_BINDING)
        new ScForBindingImpl(node)
      else if (elt == ENUMERATORS)
        new ScEnumeratorsImpl(node)
      else if (elt == GUARD)
        new ScGuardImpl(node)
      else if (elt == ARG_EXPRS)
        new ScArgumentExprListImpl(node)
      else if (elt == INTERPOLATED_PREFIX_PATTERN_REFERENCE)
        new ScInterpolatedPatternPrefix(node)

      /* Patterns */
      else if (elt == TUPLE_PATTERN)
        new ScTuplePatternImpl(node)
      else if (elt == CONSTRUCTOR_PATTERN)
        new ScConstructorPatternImpl(node)
      else if (elt == PATTERN_ARGS)
        new ScPatternArgumentListImpl(node)
      else if (elt == INFIX_PATTERN)
        new ScInfixPatternImpl(node)
      else if (elt == PATTERN)
        new ScCompositePatternImpl(node)
      else if (elt == PATTERNS)
        new ScPatternsImpl(node)
      else if (elt == WILDCARD_PATTERN)
        new ScWildcardPatternImpl(node)
      else if (elt == CASE_CLAUSE)
        new ScCaseClauseImpl(node)
      else if (elt == CASE_CLAUSES)
        new ScCaseClausesImpl(node)
      else if (elt == LITERAL_PATTERN)
        new ScLiteralPatternImpl(node)
      else if (elt == INTERPOLATION_PATTERN)
        new ScInterpolationPatternImpl(node)
      else if (elt == StableReferencePattern)
        new ScStableReferencePatternImpl(node, StableReferencePattern.toString)
      else if (elt == PATTERN_IN_PARENTHESIS)
        new ScParenthesisedPatternImpl(node)
      else if (elt == GIVEN_PATTERN)
        new ScGivenPatternImpl(node)
      else if (elt == SCALA3_TYPED_PATTERN)
        new Sc3TypedPatternImpl(node)
      /* Type patterns */
      else if (elt == TYPE_PATTERN)
        new ScTypePatternImpl(node)
      else if (elt == REFINEMENT)
        new ScRefinementImpl(node)
      /* XML */
      else if (elt == XML_EXPR)
        new ScXmlExprImpl(node)
      else if (elt == XML_START_TAG)
        new ScXmlStartTagImpl(node)
      else if (elt == XML_END_TAG)
        new ScXmlEndTagImpl(node)
      else if (elt == XML_EMPTY_TAG)
        new ScXmlEmptyTagImpl(node)
      else if (elt == XML_PI)
        new ScXmlPIImpl(node)
      else if (elt == XML_CD_SECT)
        new ScXmlCDSectImpl(node)
      else if (elt == XML_ATTRIBUTE)
        new ScXmlAttributeImpl(node)
      else if (elt == XML_PATTERN)
        new ScXmlPatternImpl(node)
      else if (elt == XML_COMMENT)
        new ScXmlCommentImpl(node)
      else if (elt == XML_ELEMENT)
        new ScXmlElementImpl(node)
      else if (elt == REFINED_TYPE)
        ???
      else if (elt == WITH_TYPE)
        ???
      else if (elt == TYPE_ARGUMENT_NAME)
        ???
      else
        new ASTWrapperPsiElement(node)
  }

  override def createFile(viewProvider: FileViewProvider): ScalaFile

  import lexer.ScalaTokenTypes._

  override def getCommentTokens = COMMENTS_TOKEN_SET

  override def getStringLiteralElements = STRING_LITERAL_TOKEN_SET

  override def getWhitespaceTokens = WHITES_SPACES_TOKEN_SET

  override def spaceExistenceTypeBetweenTokens(leftNode: ASTNode, rightNode: ASTNode): ParserDefinition.SpaceRequirements = {
    val isNeighbour = ScalaPsiUtil.getParentImportStatement(leftNode.getPsi) match {
      case null => false
      case importStatement => importStatement.getTextRange.getEndOffset == rightNode.getTextRange.getStartOffset
    }

    import ParserDefinition.SpaceRequirements._
    rightNode.getElementType match {
      case `tWHITE_SPACE_IN_LINE` if rightNode.textContains('\n') => MAY
      case _ if isNeighbour => MUST_LINE_BREAK
      case `kIMPORT` => MUST_LINE_BREAK
      case _ => MAY
    }
  }
}
