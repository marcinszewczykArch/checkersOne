name := "checkersOne"

version := "0.1"

scalaVersion := "2.13.8"

idePackagePrefix := Some("com.checkersOne")

val http4sVersion = "0.21.22"
val CirceVersion = "0.14.0-M5"
val Specs2Version  = "4.8.0"
val LogbackVersion = "1.2.3"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "org.http4s"          %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"          %% "http4s-circe"         % http4sVersion,
  "org.http4s"          %% "http4s-dsl"           % http4sVersion,
  "org.http4s"          %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s"          %% "http4s-jdk-http-client" % "0.3.6",

  "org.scalatest"       %% "scalatest"              % "3.2.12"  % Test,
  "org.scalamock"       %% "scalamock"              % "5.1.0"           % "test",

  "io.circe"            %% "circe-generic"        % CirceVersion,
//  "ch.qos.logback"      %% "logback-classic"      % "1.1.3"          % Runtime,
  "org.slf4j"           % "slf4j-nop"             % "1.7.36",
  "com.typesafe.slick"  %% "slick"                % "3.3.3",
  "com.typesafe.slick"  %% "slick-hikaricp"       % "3.3.3",
  "com.typesafe.slick"  %% "slick-codegen"        % "3.3.3",
  "mysql"               % "mysql-connector-java"  % "8.0.29",
  "com.beachape"        %% "enumeratum"           % "1.7.0",
  "org.specs2"          %% "specs2-core"          % Specs2Version % "test",
  "ch.qos.logback"      % "logback-classic"       % LogbackVersion,
)
