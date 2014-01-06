organization := "com.lunatech"

name := "play-json-evolutions"

version := "0.1.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1.3" % "provided",
  "org.specs2" %% "specs2" % "2.3.7" % "test")

publishTo <<= version { (v: String) =>
  val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
  Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
}

site.settings

site.includeScaladoc()

ghpages.settings

git.remoteRepo := "git@github.com:lunatech-labs/play2-activiti.git"

releaseSettings
