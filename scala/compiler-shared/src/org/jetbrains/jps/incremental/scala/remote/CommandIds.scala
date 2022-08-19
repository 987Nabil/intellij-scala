package org.jetbrains.jps.incremental.scala.remote

/** ATTENTION!! don't forget to update commands list in `org.jetbrains.plugins.scala.nailgun.NailgunRunner#COMMANDS` */
object CommandIds {

  final val Compile = "compile"
  final val CompileJps = "compile-jps"
  final val GetMetrics = "get-metrics"
}
