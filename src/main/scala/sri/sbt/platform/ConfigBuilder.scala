package sri.sbt.platform

import sbt.Keys._
import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin

object ConfigBuilder {

  final val IOS = "ios"

  final val ANDROID = "android"

  final val WEB = "web"

  final val SJS_OUTPUT_PATH_ANDROID = "assets/js/scalajs-output-android.js"

  final val SJS_OUTPUT_PATH_IOS = "assets/js/scalajs-output-ios.js"

  final val SJS_OUTPUT_PATH_WEB = "assets/js/scalajs-output-web.js"

  val dev =
    Def.taskKey[Unit]("Generate mobile output file for fastOptJS")

  val prod =
    Def.taskKey[Unit]("Generate mobile output file for fullOptJS")

  @inline
  def getEntryFileName(config: Configuration) =
    if (config.name == IOS) "index.ios.js"
    else if (config.name == ANDROID) "index.android.js"
    else "index.web.js"

  @inline
  def getArtifactPath(config: Configuration) =
    if (config.name == IOS) SJS_OUTPUT_PATH_IOS
    else if (config.name == ANDROID) SJS_OUTPUT_PATH_ANDROID
    else if (config.name == WEB) SJS_OUTPUT_PATH_WEB
    else "assets/scalajs-output-unknown-platform.js"

  var isServerStarted: Boolean = false

  val shell: Seq[String] =
    if (sys.props("os.name").contains("Windows")) Seq("cmd", "/c")
    else Seq("bash", "-c")
  val npmStart
    : Seq[String] = shell :+ "react-native start server --transformer scalajsTransformer.js"

  def buildConfig(config: Configuration) = {
    val aPath = getArtifactPath(config)
    val entryFile = getEntryFileName(config)
    inConfig(config)(
      Defaults.compileSettings ++
        ScalaJSPlugin.compileConfigSettings ++ Seq(
        discoveredMainClasses := (discoveredMainClasses in Compile).value,
        mainClass := (mainClass in Compile).value,
        console := (console in Compile).value,
        products := (products in Compile).value,
        classDirectory := (classDirectory in Compile).value,
        artifactPath in fastOptJS := baseDirectory.value / aPath,
        artifactPath in fullOptJS := baseDirectory.value / aPath,
        dev := {
          val indexFile = baseDirectory.value / entryFile
          IO.touch(indexFile, setModified = false)
          val indexContent = IO.read(indexFile)
          (fastOptJS in config).value.data
          val launcher = s"""require("./$aPath");"""
          if (!indexContent.contains(launcher)) IO.append(indexFile, launcher)
        },
        prod := {
          val indexFile = baseDirectory.value / entryFile
          IO.touch(indexFile, setModified = false)
          val indexContent = IO.read(indexFile)
          (fullOptJS in config).value.data
          val launcher = s"""require("./$aPath");"""
          if (!indexContent.contains(launcher)) IO.append(indexFile, launcher)
        }
      ))
  }
}
