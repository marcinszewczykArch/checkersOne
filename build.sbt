name := "checkersOne"
version := "0.1"
scalaVersion := "2.13.8"
assembly / mainClass := Some("Main")
assembly / assemblyJarName := name.value + ".jar"
assemblyMergeStrategy in assembly := {
  case PathList(ps@_*) if ps.last endsWith ".properties" => MergeStrategy.concat
  case PathList(ps@_*) if ps.last == "module-info.class" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith "Binder.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}


val http4sVersion    = "0.21.22" //"0.23.12" <- Queue from cats-effect-std
val CirceVersion     = "0.14.0-M5"
val Specs2Version    = "4.8.0"
val LogbackVersion   = "1.2.3"
val scalaTestVersion = "3.2.7.0"
val doobieVersion    = "0.13.4"  //"1.0.0-RC1"
val catsVersion      = "3.3.14"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "org.http4s"        %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"        %% "http4s-circe"         % http4sVersion,
  "org.http4s"        %% "http4s-dsl"           % http4sVersion,
  "org.http4s"        %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s"        %% "http4s-core"          % http4sVersion,
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
  "org.tpolecat"      %% "doobie-core"          % doobieVersion,
  "org.tpolecat"      %% "doobie-postgres"      % doobieVersion,
  "org.tpolecat"      %% "doobie-scalatest"     % doobieVersion    % Test
//  "org.typelevel"     %% "cats-effect"          % catsVersion,
//  "org.typelevel"     %% "cats-effect-kernel"   % catsVersion,
//  "org.typelevel"     %% "cats-effect-std"      % catsVersion
)

