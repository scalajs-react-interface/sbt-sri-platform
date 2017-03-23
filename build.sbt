name := "sbt-sri-platform"

//version := "2017.2.0-SNAPSHOT"

scalaVersion := "2.10.6"

sbtPlugin := true

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions"
)

//bintray
resolvers += Resolver.jcenterRepo

organization := "scalajs-react-interface"

licenses += ("Apache-2.0", url(
  "https://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayOrganization := Some("scalajs-react-interface")

bintrayRepository := "maven"

publishArtifact in Test := false

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.15")

//Test
resolvers += Resolver.bintrayRepo("scalajs-react-interface", "maven")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % Test
