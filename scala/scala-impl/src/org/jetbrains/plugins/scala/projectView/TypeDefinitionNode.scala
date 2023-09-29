package org.jetbrains.plugins.scala.projectView

import com.intellij.ide.projectView.{PresentationData, ViewSettings}
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScEnum, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.light.PsiClassWrapper
import org.jetbrains.plugins.scala.util.ScalaElementPresentationUtil

import java.util
import javax.swing.Icon
import scala.jdk.CollectionConverters._

private[projectView] class TypeDefinitionNode(definition: ScTypeDefinition)
                                             (implicit project: Project, settings: ViewSettings)
  extends CustomDefinitionNode(definition) {

  override final def getIcon(flags: Int): Icon = {
    val layerFlags = ScalaElementPresentationUtil.getRunnableObjectFlags(definition)
    definition.getIconWithExtraLayerFlags(flags, layerFlags)
  }

  override def updateImpl(data: PresentationData): Unit = {
    super.updateImpl(data)
    setIcon(data)
  }

  override def getTitle: String =
    validValue.fold(super.getTitle)(_.qualifiedName)

  override def getChildrenImpl: util.Collection[Node] =
    if (settings.isShowMembers)
      validValue.fold(emptyNodesList)(childrenOf)
    else
      super.getChildrenImpl

  private def childrenOf(value: ScTypeDefinition): util.List[Node] = {
    val enumCasesNodes = value match {
      case scEnum: ScEnum => scEnum.cases.map(new NamedElementNode(_))
      case _              => Nil
    }

    val memberNodes: Seq[Node] = value.members.flatMap(buildMemberNodes)
    (enumCasesNodes ++ memberNodes).asJava
  }

  override def getPsiClass: PsiClass =
    validValue.filter(_.isObject)
      .fold(super.getPsiClass) { definition =>
        new PsiClassWrapper(definition, definition.qualifiedName, definition.name)
      }
}
