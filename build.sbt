ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "mongo_fs2"

lazy val root = (project in file("."))
  .settings(
    name := "Mongo FS2",
    outputStrategy := Some(StdoutOutput),
    fork := true,
    libraryDependencies ++= List(
      "org.typelevel" %% "cats-effect" % "3.2.9",
      "co.fs2" %% "fs2-core" % "3.1.3",
      "co.fs2" %% "fs2-reactive-streams" % "3.1.3",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.2"
    )
  )
