//import AssemblyKeys._
//import sbtassembly.Plugin._

name := "flocking"

def settings = Seq (
  version := "0.1-SNAPSHOT",
  scalaVersion := "3.0.1"
)

lazy val model = Project("model", file("model")) settings(settings:_*)
lazy val visu = Project("visu", file("visu")) settings(settings:_*) settings(
  libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
) dependsOn(model)



//scalacOptions ++= Seq("-deprecation", "-feature")


/*resolvers ++= Seq(
  "ISC-PIF Release" at "http://maven.iscpif.fr/public/",
  "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)*/

//libraryDependencies ++= Seq(
//  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test"
//)

//libraryDependencies += "org.scala-lang" % "scala-swing" % "2.11+"

//libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

//libraryDependencies += "fr.iscpif" %% "mgo" % "1.72-SNAPSHOT"

//assemblySettings

//jarName in assembly := "flocking.jar"

