name := "flocking"

def settings = Seq (
  version := "0.1-SNAPSHOT",
  scalaVersion := "3.3.1"
)

lazy val model = Project("model", file("model")).settings(settings:_*).enablePlugins(SbtOsgi).settings (
  OsgiKeys.exportPackage := Seq("flocking.*;-split-package:=merge-first"),
  OsgiKeys.importPackage := Seq("*;resolution:=optional"),
  OsgiKeys.privatePackage := Seq("!scala.*,!java.*,!META-INF.*.RSA,!META-INF.*.SF,!META-INF.*.DSA,META-INF.services.*,META-INF.*,*"),
  OsgiKeys.requireCapability := """osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.8))""""
)

lazy val visu = Project("visu", file("visu")) settings(settings:_*) settings(
  libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
) dependsOn(model)

