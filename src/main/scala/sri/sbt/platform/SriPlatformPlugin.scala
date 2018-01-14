package sri.sbt.platform

import ConfigBuilder._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys.{defaultConfiguration, ivyConfigurations}
import sbt.{AutoPlugin, Global, config, overrideConfigs}
import sbt._
import sbt.Keys._

object SriPlatformPlugin extends AutoPlugin {
  override lazy val requires = ScalaJSPlugin
  object autoImport {
    lazy val common = config("common")
    lazy val ios = config(IOS) extend (common)
    lazy val android = config(ANDROID) extend (common)
    lazy val web = config(WEB) extend (common)
    lazy val CustomCompile = config("compile") extend (ios, android, common)
  }

  import autoImport._

  override def projectSettings: Seq[_root_.sbt.Def.Setting[_]] =
    Seq(
      defaultConfiguration := Some(common),
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
      ivyConfigurations := overrideConfigs(ios,
                                           android,
                                           web,
                                           common,
                                           CustomCompile)(
        ivyConfigurations.value),
      scalaJSUseMainModuleInitializer := true
    ) ++ buildConfig(android) ++ buildConfig(ios) ++ buildConfig(web)
}
