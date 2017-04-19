package sri.sbt.platform

import org.scalajs.core.tools.linker.backend.ModuleKind
import sbt.Keys._
import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._
import org.scalajs.sbtplugin.ScalaJSPluginInternal
import org.scalajs.sbtplugin.ScalaJSPluginInternal.scalaJSProjectBaseSettings

object ConfigBuilder {

  final val IOS = "ios"

  final val ANDROID = "android"

  final val WEB = "web"

  final val PLATFORM_CONFIG_IOS = "platform-config-ios"

  final val PLATFORM_CONFIG_ANDROID = "platform-config-android"

  final val PLATFORM_CONFIG_WEB = "platform-config-web"

  final val SJS_OUTPUT_PATH_ANDROID = "assets/scalajs-output-android.js"

  final val SJS_OUTPUT_PATH_IOS = "assets/scalajs-output-ios.js"

  final val SJS_OUTPUT_PATH_WEB = "assets/scalajs-output-web.js"

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
    else SJS_OUTPUT_PATH_WEB

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
        ScalaJSPluginInternal.scalaJSCompileSettings ++ Seq(
        discoveredMainClasses := (discoveredMainClasses in Compile).value,
        mainClass := (mainClass in Compile).value,
        console := (console in Compile).value,
        products := (products in Compile).value,
        classDirectory := (classDirectory in Compile).value,
        artifactPath in fastOptJS := baseDirectory.value / aPath,
        artifactPath in fullOptJS := baseDirectory.value / aPath,
        dev := {
          val indexFile = baseDirectory.value / entryFile
          val indexContent = IO.read(indexFile)
          (fastOptJS in config).value.data
          val launcher = s"""require("./$aPath");"""
          if (!indexContent.contains(launcher)) IO.append(indexFile, launcher)
        },
        prod := {
          val indexFile = baseDirectory.value / entryFile
          val indexContent = IO.read(indexFile)
          (fullOptJS in config).value.data
          val launcher = s"""require("./$aPath");"""
          if (!indexContent.contains(launcher)) IO.append(indexFile, launcher)
        }
      ))
  }
}
