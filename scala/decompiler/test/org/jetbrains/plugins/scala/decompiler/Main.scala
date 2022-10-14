package org.jetbrains.plugins.scala.decompiler

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.jar.JarInputStream
import scala.util.chaining.scalaUtilChainingOps

object Main {
  sealed abstract class Mode
  object Mode {
    case object Parse extends Mode
    case object Test extends Mode
    case object Benchmark extends Mode
  }

  private val mode: Mode = Mode.Test

  private val Home: String = System.getProperty("user.home")

  private val Repository = Home + "/.cache/coursier/v1/https/repo1.maven.org/maven2/"
  private val OutputDir = Home + "/IdeaProjects/scala-plugin-for-ultimate/community/scala/decompiler/target/comparison"

  // scalaVersion := "2.13.9",

  // libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",

  // libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14",

  // libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.17.0",

  // libraryDependencies += "dev.zio" %% "zio" % "2.0.2",
  // libraryDependencies += "dev.zio" %% "zio-streams" % "2.0.2",

  // libraryDependencies += "org.typelevel" %% "cats-core" % "2.8.0",
  // libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.14",

  // libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.13.9",

  // libraryDependencies += "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",

  // libraryDependencies += "io.getquill" %% "quill-sql" % "4.6.0",
  // libraryDependencies += "io.getquill" %% "quill-jdbc-zio" % "4.6.0"

  private val Libraries = Seq(
    "org/scala-lang/scala-library/2.13.9/scala-library-2.13.9.jar",
    "org/scala-lang/scala-reflect/2.13.9/scala-reflect-2.13.9.jar",
    "org/scala-lang/scala-compiler/2.13.9/scala-compiler-2.13.9.jar",

    "org/scalatest/scalatest-core_2.13/3.2.14/scalatest-core_2.13-3.2.14.jar",
    "org/scalatest/scalatest-funspec_2.13/3.2.14/scalatest-funspec_2.13-3.2.14.jar",
    "org/scalatest/scalatest-funsuite_2.13/3.2.14/scalatest-funsuite_2.13-3.2.14.jar",

    "org/scalactic/scalactic_2.13/3.2.14/scalactic_2.13-3.2.14.jar",

    "org/scalacheck/scalacheck_2.13/1.17.0/scalacheck_2.13-1.17.0.jar",

    "dev/zio/zio_2.13/2.0.2/zio_2.13-2.0.2.jar",
    "dev/zio/zio-streams_2.13/2.0.2/zio-streams_2.13-2.0.2.jar",

    "org/typelevel/cats-core_2.13/2.8.0/cats-core_2.13-2.8.0.jar",
    "org/typelevel/cats-kernel_2.13/2.8.0/cats-kernel_2.13-2.8.0.jar",
    "org/typelevel/cats-effect_2.13/3.3.14/cats-effect_2.13-3.3.14.jar",
    "org/typelevel/cats-effect-kernel_2.13/3.3.14/cats-effect-kernel_2.13-3.3.14.jar",
    "org/typelevel/cats-effect-std_2.13/3.3.14/cats-effect-std_2.13-3.3.14.jar",

    "io/getquill/quill-sql_2.13/4.6.0/quill-sql_2.13-4.6.0.jar",
    "io/getquill/quill-jdbc-zio_2.13/4.6.0/quill-jdbc-zio_2.13-4.6.0.jar",

    "org/tpolecat/doobie-core_2.13/1.0.0-RC1/doobie-core_2.13-1.0.0-RC1.jar",
  )

  def main(args: Array[String]): Unit = {
    assert(new File(OutputDir).getParentFile.exists)

    val start = System.currentTimeMillis()

    Libraries.sortBy(_.split('/').last).foreach { binaries =>
      println("Parsing signatures:\t\t" + binaries)
      new JarInputStream(new BufferedInputStream(new FileInputStream(Repository + "/" + binaries))).pipe { in =>
        Iterator.continually(in.getNextEntry).takeWhile(_ != null).filter(_.getName.endsWith(".class")).foreach { entry =>
          val file = new File(s"$OutputDir/${entry.getName}")
          val path = Paths.get(file.getPath.replaceFirst("\\.class$", ".scala"))
          mode match {
            case Mode.Parse =>
              file.getParentFile.mkdirs()
              Decompiler.sourceNameAndText(entry.getName, in.readAllBytes()) match {
                case Some((_, text)) => Files.write(path, text.getBytes)
                case None =>
              }
            case Mode.Test =>
              if (path.toFile.exists()) { // Inner classes don't have separate source files
                val expected = new String(Files.readAllBytes(path))
                Decompiler.sourceNameAndText(entry.getName, in.readAllBytes()) match {
                  case Some((_, actual)) =>
                    val actualPath = Path.of(path.toString.replaceFirst("\\.scala$", ".actual.scala"))
                    if (expected != actual) {
                      System.err.println(path.toString.substring(OutputDir.length + 1))
                      Files.write(actualPath, actual.getBytes)
                    } else {
                      val actualFile = actualPath.toFile
                      if (actualFile.exists()) {
                        actualFile.delete()
                      }
                    }
                  case None =>
                }
              }
            case Mode.Benchmark =>
              Decompiler.sourceNameAndText(entry.getName, in.readAllBytes())
          }
        }
      }

      val sources = binaries.replaceFirst("\\.jar$", "-sources.jar")

      if (mode == Mode.Parse) {
        println("Extracting sources:\t" + sources)
        new JarInputStream(new BufferedInputStream(new FileInputStream(Repository + "/" + sources))).pipe { in =>
          Iterator.continually(in.getNextEntry).takeWhile(_ != null).filter(_.getName.endsWith(".scala")).foreach { entry =>
            val file = new File(s"$OutputDir/${entry.getName}")
            file.getParentFile.mkdirs()
            val s = new String(in.readAllBytes) // TODO store pre-compiled regex
              .replaceAll(raw"(?m)^\s*import.*?$$", "") // Import
              .replaceAll(raw"\s*//.*?\n", "") // Line comment
              .replaceAll(raw"(?s)/\*.*?\*/", "") // Block comment
              .replaceAll(raw"(?m)^\s+$$", "") // Whitespaces on empty line
              .replaceAll(raw"\n{3,}", "\n\n") // Multiple empty lines
              .replaceAll(raw"\{\n\n", "{\n") // Empty line after {
              .replaceAll(raw"\n\n}", "\n}") // Empty line before }
              .trim

            Files.write(Paths.get(file.getPath.replaceFirst("\\.scala$", "-source.scala")), s.getBytes)
          }
        }
      }
    }

    if (mode == Mode.Benchmark) {
      printf("Elapsed: %.2f s.\n", (System.currentTimeMillis() - start) / 1000.0D)
    } else {
      println("Done:\t\t\t\t" + OutputDir)
    }
  }
}
