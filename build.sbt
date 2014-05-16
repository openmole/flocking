import AssemblyKeys._
import sbtassembly.Plugin._


name := "flocking"

version := "0.1"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "ISC-PIF Release" at "http://maven.iscpif.fr/public/"
)

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.1" % "test"
)

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.3"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.3"

libraryDependencies += "fr.iscpif" %% "mgo" % "1.70-SNAPSHOT"

assemblySettings

jarName in assembly := "flocking.jar"

