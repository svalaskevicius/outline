enablePlugins(JavaAppPackaging, DockerPlugin)
import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "3.7.3"

lazy val root = (project in file("."))
  .settings(
    name := "imagen",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % "1.0.0-M46",
      "org.http4s" %% "http4s-ember-server" % "1.0.0-M46",
      "org.http4s" %% "http4s-circe" % "1.0.0-M46",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6"
    ),
    mainClass := Some("Main"),
    dockerBaseImage := "openjdk:17-slim",
    dockerExposedPorts := Seq(5000),
    dockerBaseImage := "imagen-base:2025-10-24T15-00-20",
  )
