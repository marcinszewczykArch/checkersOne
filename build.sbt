name := "checkersOne"
version := "0.1"
scalaVersion := "2.13.8"

val http4sVersion    = "0.21.22" //"0.23.12" <- no Queue in this version
val CirceVersion     = "0.14.0-M5"
val Specs2Version    = "4.8.0"
val LogbackVersion   = "1.2.3"
val scalaTestVersion = "3.2.7.0"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "org.http4s"        %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"        %% "http4s-circe"         % http4sVersion,
  "org.http4s"        %% "http4s-dsl"           % http4sVersion,
  "org.http4s"        %% "http4s-blaze-client"  % http4sVersion,
//  "org.http4s"          %% "http4s-jdk-http-client" % "0.3.6",

  "org.scalactic"     %% "scalactic"            % "3.2.12",
  "org.scalatest"     %% "scalatest"            % "3.2.12"         % "test",
  "org.scalamock"     %% "scalamock"            % "5.1.0"          % "test",
  "org.scalatestplus" %% "scalacheck-1-15"      % scalaTestVersion % Test,
  "org.scalatestplus" %% "selenium-3-141"       % scalaTestVersion % Test,

  "io.circe"          %% "circe-generic"        % CirceVersion,
  "org.slf4j"          % "slf4j-nop"            % "1.7.36",
  "mysql"              % "mysql-connector-java" % "8.0.29",
  "com.beachape"      %% "enumeratum"           % "1.7.0",
  "org.specs2"        %% "specs2-core"          % Specs2Version    % "test",
  "ch.qos.logback"     % "logback-classic"      % LogbackVersion,

//  "org.typelevel"       %% "cats-effect"            % "3.3.14"  //<- http4sVersion "0.23.12" required
)
