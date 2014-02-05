name := "ncso-website"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.openrdf.sesame" % "sesame-runtime" % "2.6.10",
  "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.0.2",
  jdbc,
  anorm,
  cache
)

play.Project.playScalaSettings
