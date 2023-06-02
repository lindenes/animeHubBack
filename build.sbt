Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / version    := "0.1.0-SNAPSHOT"
ThisBuild / bspEnabled := true

addCommandAlias(
  "dependencyCheck",
  List(
    "reload plugins",    // check SBT plugin updates
    "dependencyUpdates",
    "reload return",
    "dependencyUpdates", // check project deps updates
    "undeclaredCompileDependencies",
    "unusedCompileDependencies",
  ).mkString(";"),
)

val versions = new {
  val cats               = "2.9.0"
  val catsEffect         = "3.5.0"
  val circe              = "0.14.5"
  val doobie             = "1.0.0-RC2"
  val fs2                = "3.7.0"
  val http4s             = "1.0.0-M39"
  val ip4s               = "3.3.0"
  val logback            = "1.4.7"
  val mysqlConnectorJava = "8.0.33"
  val scala              = "3.3.1-RC1"
  val slf4j              = "2.0.7"
}

val validate = taskKey[Unit]("Validate everything.")
val massage  = taskKey[Unit]("Apply automatic refactoring and rewrites.")

lazy val root = (project in file(".")).settings(
  name                := "animeHubBack",
  libraryDependencies := List(
    "ch.qos.logback" % "logback-classic"      % versions.logback            % Runtime,
    "co.fs2"        %% "fs2-core"             % versions.fs2,
    "co.fs2"        %% "fs2-io"               % versions.fs2,
    "com.comcast"   %% "ip4s-core"            % versions.ip4s,
    "io.circe"      %% "circe-core"           % versions.circe,
    "io.circe"      %% "circe-literal"        % versions.circe,
    "mysql"          % "mysql-connector-java" % versions.mysqlConnectorJava % Runtime,
    "org.http4s"    %% "http4s-circe"         % versions.http4s,
    "org.http4s"    %% "http4s-core"          % versions.http4s,
    "org.http4s"    %% "http4s-dsl"           % versions.http4s,
    "org.http4s"    %% "http4s-ember-server"  % versions.http4s,
    "org.http4s"    %% "http4s-server"        % versions.http4s,
    "org.slf4j"      % "slf4j-api"            % versions.slf4j              % Runtime,
    "org.tpolecat"  %% "doobie-core"          % versions.doobie,
    "org.tpolecat"  %% "doobie-free"          % versions.doobie,
    "org.typelevel" %% "cats-core"            % versions.cats,
    "org.typelevel" %% "cats-effect"          % versions.catsEffect,
    "org.typelevel" %% "cats-effect-kernel"   % versions.catsEffect,
    "org.typelevel" %% "cats-free"            % versions.cats,
    "org.typelevel" %% "cats-kernel"          % versions.cats,
  ),
  scalacOptions       := List(
    "-Werror",
    "-Wnonunit-statement",
    "-Wunused:all",
    "-Wvalue-discard",
    "-Yno-experimental",
    "-Ysafe-init",
    "-deprecation",
    "-feature",
    "-new-syntax",
    "-unchecked",
  ),
  scalaVersion        := versions.scala,
  semanticdbEnabled   := true,
  validate            := {
    (Test / test).value
    (Compile / scalafmtSbtCheck).value
    (Compile / scalafmtCheckAll).value
    (Test / scalafmtCheckAll).value
    Project.extract(state.value).runInputTask(scalafixAll, " --check", state.value)._1
    undeclaredCompileDependenciesTest.value
    unusedCompileDependenciesTest.value
  },
  massage             := {
    (Compile / scalafmtSbt).value
    (Compile / scalafmtAll).value
    (Test / scalafmtAll).value
    Project.extract(state.value).runInputTask(scalafixAll, "", state.value)._1
    (Test / compile).value
  },
)
