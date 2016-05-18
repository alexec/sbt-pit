sbtPlugin := true

name := "sbt-pit"

organization := "org.pitest.sbt"

version := "1.1.10-SNAPSHOT"

crossScalaVersions := Seq("2.11.3")

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

val pitVersion = "1.1.10"

libraryDependencies += "org.pitest" % "pitest" % pitVersion

libraryDependencies += "org.pitest" % "pitest-html-report" % pitVersion

publishTo <<= version { version: String =>
   val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
   val (name, url) = if (version.contains("-SNAPSHOT"))
     ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
   else
     ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
   Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}

publishMavenStyle := false

