name := "de-htwg-mps-battleship"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

lazy val battleship = RootProject(uri("git://github.com/magoeke/de.htwg.mps.battleship.git#master"))

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.5.0", 
  "org.webjars" % "bootstrap" % "2.3.1"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(battleship)
