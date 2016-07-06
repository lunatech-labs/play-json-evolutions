organization := "com.lunatech"

name := "play-json-evolutions"

version := "0.1.3"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", "2.11.8")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.5.4" % "provided",
  "org.specs2"        %% "specs2-core" % "3.8.4" % "test")

publishTo <<= version { (v: String) =>
  val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
  Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
}

site.settings

site.includeScaladoc()

ghpages.settings

git.remoteRepo := "git@github.com:lunatech-labs/play-json-evolutions.git"

releaseSettings
