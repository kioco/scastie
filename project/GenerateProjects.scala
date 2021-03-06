import java.nio.file._

import com.olegych.scastie.api._
import com.olegych.scastie.buildinfo.BuildInfo

class GenerateProjects(sbtTargetDir: Path) {
  val projectTarget: Path = sbtTargetDir.resolve("projects")

  val projects: List[GeneratedProject] = {
    val helloWorld =
      """|object Main {
         |  def main(args: Array[String]): Unit = {
         |    println("Hello, World!")
         |  }
         |}
         |""".stripMargin

    val default = Inputs.default.copy(
        code = helloWorld,
      )

    def scala(version: String): Inputs = default.copy(
      target = ScalaTarget.Jvm(version),
    )

    val scala212 = scala(BuildInfo.latest212)
    val scala213 = scala(BuildInfo.latest213)

    val dotty = default.copy(
      target = ScalaTarget.Dotty.default
    )

    val scalaJs = default.copy(
      target = ScalaTarget.Js.default,
    )

    List(
      (scala212, "scala212"),
      (scala213, "scala213"),
      (dotty, "dotty"),
      (scalaJs, "scalaJs")
    ).map {
      case (inputs, name) =>
        new GeneratedProject(
          inputs,
          projectTarget.resolve(name)
        )
    }
  }

  def generateSbtProjects(): Unit =
    projects.foreach(_.generateSbtProject)
}

class GeneratedProject(inputs: Inputs, sbtDir: Path) {
  private val buildFile = sbtDir.resolve("build.sbt")
  private val projectDir = sbtDir.resolve("project")
  private val pluginFile = projectDir.resolve("plugins.sbt")
  private val codeFile = sbtDir.resolve("src/main/scala/main.scala")

  def generateSbtProject(): Unit = {
    Files.createDirectories(projectDir)

    Files.write(
      projectDir.resolve("build.properties"),
      s"sbt.version = ${BuildInfo.sbtVersion}".getBytes
    )

    Files.write(buildFile, inputs.sbtConfig.getBytes)
    Files.write(pluginFile, inputs.sbtPluginsConfig.getBytes)

    Files.createDirectories(codeFile.getParent)
    Files.write(codeFile, inputs.code.getBytes)
  }

  def runCmd(dest: String): String = {
    val dir = sbtDir.getFileName

    s"""cd $dest/$dir && sbt "${inputs.target.sbtRunCommand}""""
  }
}
