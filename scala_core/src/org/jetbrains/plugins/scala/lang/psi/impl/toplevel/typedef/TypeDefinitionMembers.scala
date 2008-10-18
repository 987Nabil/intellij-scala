/**
* @author ven
*/
package org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef

import _root_.scala.collection.mutable.HashSet
import api.base.{ScFieldId, ScPrimaryConstructor}
import api.toplevel.templates.ScExtendsBlock
import com.intellij.psi.scope.{PsiScopeProcessor, ElementClassHint}
import com.intellij.psi._
import synthetic.{SyntheticClasses, ScSyntheticClass}
import types._
import api.toplevel.typedef._
import api.statements._
import types.PhysicalSignature
import _root_.scala.collection.mutable.ListBuffer
import com.intellij.openapi.util.Key
import util._
import _root_.scala.collection.mutable.HashMap

object TypeDefinitionMembers {
  object MethodNodes extends MixinNodes {
    type T = Signature
    def equiv(s1: Signature, s2: Signature) = s1 equiv s2
    def computeHashCode(s: Signature) = s.name.hashCode * 31 + s.types.length
    def isAbstract(s: Signature) = s.isAbstract

    def processJava(clazz: PsiClass, subst: ScSubstitutor, map: Map) =
      for (method <- clazz.getMethods) {
        val sig = new PhysicalSignature(method, subst)
        map += ((sig, new Node(sig, subst)))
      }

    def processScala(template : ScTemplateDefinition, subst: ScSubstitutor, map: Map) =
      for (member <- template.members) {
        member match {
          case method: ScFunction => {
            val sig = new PhysicalSignature(method, subst)
            map += ((sig, new Node(sig, subst)))
          }
          case _var: ScVariable =>
            for (dcl <- _var.declaredElements) {
              val t = dcl.calcType
              val getter = new Signature(dcl.name, Seq.empty, t, subst)
              map += ((getter, new Node(getter, subst)))
              val setter = new Signature(dcl.name + "_", Seq.singleton(t), Unit, subst)
              map += ((setter, new Node(setter, subst)))
            }
          case _val: ScValue =>
            for (dcl <- _val.declaredElements) {
              val getter = new Signature(dcl.name, Seq.empty, dcl.calcType, subst)
              map += ((getter, new Node(getter, subst)))
            }
          case constr : ScPrimaryConstructor =>
            for (p <- constr.parameters) {
              val isvar = p.isVar
              val isval = p.isVal
              if (isval || isvar) {
                val t = p.calcType
                val getter = new Signature(p.name, Seq.empty, t, subst)
                map += ((getter, new Node(getter, subst)))
                if (isvar) {
                  val setter = new Signature(p.name + "_", Seq.singleton(t), Unit, subst)
                  map += ((setter, new Node(setter, subst)))
                }
              }
            }
          case _ =>
        }
      }
  }

  import com.intellij.psi.PsiNamedElement

  object ValueNodes extends MixinNodes {
    type T = PsiNamedElement
    def equiv(n1: PsiNamedElement, n2: PsiNamedElement) = n1.getName == n2.getName
    def computeHashCode(named: PsiNamedElement) = named.getName.hashCode
    def isAbstract(named: PsiNamedElement) = named match {
      case _: ScFieldId => true
      case f: PsiField if f.hasModifierProperty(PsiModifier.ABSTRACT) => true
      case _ => false
    }

    def processJava(clazz: PsiClass, subst: ScSubstitutor, map: Map) =
      for (field <- clazz.getFields) {
        map += ((field, new Node(field, subst)))
      }

    def processScala(template : ScTemplateDefinition, subst: ScSubstitutor, map: Map) =
      for (member <- template.members) {
        member match {
          case obj: ScObject => map += ((obj, new Node(obj, subst)))
          case _var: ScVariable =>
            for (dcl <- _var.declaredElements) {
              map += ((dcl, new Node(dcl, subst)))
            }
          case _val: ScValue =>
            for (dcl <- _val.declaredElements) {
              map += ((dcl, new Node(dcl, subst)))
            }
          case constr : ScPrimaryConstructor =>
            for (param <- constr.parameters) {
              map += ((param, new Node(param, subst)))
            }
          case _ =>
        }
      }
  }

  import org.jetbrains.plugins.scala.lang.psi.api.statements.ScTypeAlias

  object TypeNodes extends MixinNodes {
    type T = PsiNamedElement //class or type alias
    def equiv(t1: PsiNamedElement, t2: PsiNamedElement) = t1.getName == t2.getName
    def computeHashCode(t: PsiNamedElement) = t.getName.hashCode
    def isAbstract(t: PsiNamedElement) = t match {
      case _: ScTypeAliasDeclaration => true
      case _ => false
    }

    def processJava(clazz: PsiClass, subst: ScSubstitutor, map: Map) =
      for (inner <- clazz.getInnerClasses) {
        map += ((inner, new Node(inner, subst)))
      }

    def processScala(template : ScTemplateDefinition, subst: ScSubstitutor, map: Map) = {
      for (member <- template.members) {
        member match {
          case alias: ScTypeAlias => map += ((alias, new Node(alias, subst)))
          case _ : ScObject =>
          case td : ScTypeDefinition=> map += ((td, new Node(td, subst)))
          case _ =>
        }
      }
    }
  }

  import ValueNodes.{Map => VMap}, MethodNodes.{Map => MMap}, TypeNodes.{Map => TMap}
  val valsKey: Key[CachedValue[(VMap, VMap)]] = Key.create("vals key")
  val methodsKey: Key[CachedValue[HashSet[PhysicalSignature]]] = Key.create("vals key")
  val signaturesKey: Key[CachedValue[(MMap, MMap)]] = Key.create("methods key")
  val typesKey: Key[CachedValue[(TMap, TMap)]] = Key.create("types key")

  def getVals(clazz: PsiClass) = get(clazz, valsKey, new MyProvider(clazz, { clazz : PsiClass => ValueNodes.build(clazz) }))._2
  def getSignatures(clazz: PsiClass) = get(clazz, signaturesKey, new MyProvider(clazz, { clazz : PsiClass => MethodNodes.build(clazz) }))._2
  def getTypes(clazz: PsiClass) = get(clazz, typesKey, new MyProvider(clazz, { clazz : PsiClass => TypeNodes.build(clazz) }))._2

  def getMethods(clazz: PsiClass) = get(clazz, methodsKey, new MyProvider(clazz, { clazz : PsiClass =>
          val set = new HashSet[PhysicalSignature]
          for (sig <- getSignatures(clazz)) {
            sig match {
              case phys : PhysicalSignature => set += phys
              case _ =>
            }
          }
          set
  }))
  
  def getSuperVals(c: PsiClass) = get(c, valsKey, new MyProvider(c, { c : PsiClass => ValueNodes.build(c) }))._1
  def getSuperMethods(c: PsiClass) = get(c, signaturesKey, new MyProvider(c, { c : PsiClass => MethodNodes.build(c) }))._1
  def getSuperTypes(c: PsiClass) = get(c, typesKey, new MyProvider(c, { c : PsiClass => TypeNodes.build(c) }))._1

  private def get[Dom <: PsiElement, T](e: Dom, key: Key[CachedValue[T]], provider: => CachedValueProvider[T]) = {
    var computed = e.getUserData(key)
    if (computed == null) {
      val manager = PsiManager.getInstance(e.getProject).getCachedValuesManager
      computed = manager.createCachedValue(provider, false)
      e.putUserData(key, computed)
    }
    computed.getValue
  }

  class MyProvider[Dom, T](e: Dom, builder: Dom => T) extends CachedValueProvider[T] {
    def compute() = new CachedValueProvider.Result(builder(e),
    Array[Object](PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT))
  }

  def processDeclarations(clazz : PsiClass,
                          processor: PsiScopeProcessor,
                          state: ResolveState,
                          lastParent: PsiElement,
                          place: PsiElement) : Boolean =
    processDeclarations(processor, state, lastParent, place, getVals(clazz), getSignatures(clazz), getTypes(clazz)) &&
    AnyRef.asClass(clazz.getProject).processDeclarations(processor, state, lastParent, place) &&
    Any.asClass(clazz.getProject).processDeclarations(processor, state, lastParent, place)

  def processSuperDeclarations(clazz : PsiClass,
                          processor: PsiScopeProcessor,
                          state: ResolveState,
                          lastParent: PsiElement,
                          place: PsiElement) : Boolean =
    processDeclarations(processor, state, lastParent, place, getSuperVals(clazz), getSuperMethods(clazz), getSuperTypes(clazz))

  private def processDeclarations(processor: PsiScopeProcessor,
                                  state: ResolveState,
                                  lastParent: PsiElement,
                                  place: PsiElement,
                                  vals: => ValueNodes.Map,
                                  methods: => MethodNodes.Map,
                                  types: => TypeNodes.Map) : Boolean = {
    val substK = state.get(ScSubstitutor.key)
    val subst = if (substK == null) ScSubstitutor.empty else substK
    if (shouldProcessVals(processor)) {
      for ((_, n) <- vals) {
        if (!processor.execute(n.info, state.put(ScSubstitutor.key, n.substitutor followed subst))) return false
      }
    }
    if (shouldProcessMethods(processor)) {
      for ((_, n) <- methods) {
        n.info match {
          case phys : PhysicalSignature =>
            if (!processor.execute(phys.method, state.put(ScSubstitutor.key, n.substitutor followed subst))) return false
          case _ => 
        }
      }
    }
    if (shouldProcessTypes(processor)) {
      for ((_, n) <- types) {
        if (!processor.execute(n.info, state.put(ScSubstitutor.key, n.substitutor followed subst))) return false
      }
    }

    true
  }

  import scala.lang.resolve._, scala.lang.resolve.ResolveTargets._

  def shouldProcessVals(processor: PsiScopeProcessor) = processor match {
    case BaseProcessor(kinds) => (kinds contains VAR) || (kinds contains VAL) || (kinds contains OBJECT)
    case _ => {
      val hint = processor.getHint(classOf[ElementClassHint])
      hint == null || hint.shouldProcess(classOf[PsiVariable])
    }
  }

  def shouldProcessMethods(processor: PsiScopeProcessor) = processor match {
    case BaseProcessor(kinds) => kinds contains METHOD
    case _ => {
      val hint = processor.getHint(classOf[ElementClassHint])
      hint == null || hint.shouldProcess(classOf[PsiMethod])
    }
  }

  def shouldProcessTypes(processor: PsiScopeProcessor) = processor match {
    case BaseProcessor(kinds) => kinds contains CLASS
    case _ => false //important: do not process inner classes!
  }
}