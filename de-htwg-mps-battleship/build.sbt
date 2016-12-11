name := "de-htwg-mps-battleship"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.5.0", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars.bower" % "github-com-PolymerElements-paper-elements" % "1.0.7",
  "org.webjars" % "polymer" % "1.6.1"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

fork in run := false
