ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

val http4sVersion = "1.0.0-M39"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.6",
  "org.slf4j" % "slf4j-api" % "2.0.5",
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.14.5",
  "io.circe" %% "circe-literal" % "0.14.5",
  "mysql" % "mysql-connector-java" % "8.0.32",
  "org.tpolecat" %% "doobie-core" % "1.0.0-M5",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-M5",
  "org.http4s" %% "http4s-session" % http4sVersion

)

lazy val root = (project in file("."))
  .settings(
    name := "animeHubBack",
    mainClass := Some("Main")
  )
