name := "checkersOne"

version := "0.1"

scalaVersion := "2.13.8"

val Http4sVersion = "1.0.0-M21"
val CirceVersion = "0.14.0-M5"

libraryDependencies ++= Seq(
  "org.http4s"          %% "http4s-blaze-server"  % Http4sVersion,
  "org.http4s"          %% "http4s-circe"         % Http4sVersion,
  "org.http4s"          %% "http4s-dsl"           % Http4sVersion,
  "io.circe"            %% "circe-generic"        % CirceVersion,
//  "ch.qos.logback"      %% "logback-classic"      % "1.1.3"          % Runtime,
  "org.slf4j"           % "slf4j-nop"             % "1.7.36",
  "com.typesafe.slick"  %% "slick"                % "3.3.3",
  "com.typesafe.slick"  %% "slick-hikaricp"       % "3.3.3",
  "com.typesafe.slick"  %% "slick-codegen"        % "3.3.3",
  "mysql"               % "mysql-connector-java"  % "8.0.29",
)
