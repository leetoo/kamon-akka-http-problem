// *****************************************************************************
// Projects
// *****************************************************************************

import com.typesafe.sbt.packager.docker._

scalafmtOnCompile in ThisBuild := true
scalafmtTestOnCompile in ThisBuild := true
scalafmtFailTest in ThisBuild := false

scalaVersion := "2.12.4"

version in ThisBuild ~= (_.replace('+', '-'))
dynver in ThisBuild ~= (_.replace('+', '-'))

resolvers += Resolver.bintrayRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % "1.0.4")

lazy val app = (project in file("app"))
  .configs(IntegrationTest)
  .enablePlugins(JavaAppPackaging, DockerPlugin, JavaAgent)
  .settings(dockerSettings)
  .settings(Defaults.itSettings)
  .settings(
    mainClass in assembly := Some("lukasz-golebiewski.App"),
    assemblyJarName in assembly := "app.jar",
    test in assembly := {},
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.8.13" % "runtime",
    javaOptions in Universal += "-Dorg.aspectj.tracing.factory=default",
    javaOptions in reStart ++= (javaOptions in run).value
  )
  .settings(
    name := "app",
    commonSettings,
    configAnnotationSettings,
    libraryDependencies ++= Seq(
      library.akkaActor,
      library.akkaHttp,
      library.akkaSlf4j,
      library.akkaStream,
      library.googlePubSub,
      library.logback,
      library.logbackEncoder,
      library.scalaLogging,
      library.scalatest,
      library.slf4jApi,
      library.akkaStreamTestkit,
      library.akkaHttpTestkit,
      library.akkaHttpSprayJson,
      library.kamonCore,
      library.kamonAkkaHttp,
      library.kamonPrometheus,
      library.kamonSystemMetrics
    )
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************
lazy val library =
  new {
    object Version {
      final val akka                  = "2.5.9"
      final val akkaHttp              = "10.0.11"
      final val kamon                 = "1.0.0"
      final val sprayJson             = "1.3.4"
      final val wacaiConfigAnnotation = "0.3.7"
      final val macroParadise         = "2.1.1"
      final val scalaLogging          = "3.7.2"
      final val slf4jApi              = "1.7.25"
      final val logback               = "1.2.3"
      final val logbackEncoder        = "4.11"
      final val googleCloudBigQuery   = "0.32.0-beta"
      final val googlePubSub          = "0.32.0-beta"
      final val scalatest             = "3.0.5"
      final val scalacheck            = "1.13.5"
    }
    val akkaActor           = "com.typesafe.akka"          %% "akka-actor"              % Version.akka
    val akkaStream          = "com.typesafe.akka"          %% "akka-stream"             % Version.akka
    val akkaSlf4j           = "com.typesafe.akka"          %% "akka-slf4j"              % Version.akka
    val akkaStreamTestkit   = "com.typesafe.akka"          %% "akka-stream-testkit"     % Version.akka
    val akkaHttp            = "com.typesafe.akka"          %% "akka-http"               % Version.akkaHttp
    val akkaHttpSprayJson   = "com.typesafe.akka"          %% "akka-http-spray-json"    % Version.akkaHttp
    val akkaHttpTestkit     = "com.typesafe.akka"          %% "akka-http-testkit"       % Version.akkaHttp % "test"
    val sprayJson           = "io.spray"                   %% "spray-json"              % Version.sprayJson
    val googlePubSub        = "com.google.cloud"           % "google-cloud-pubsub"      % Version.googlePubSub
    val googleCloudBigQuery = "com.google.cloud"           % "google-cloud-bigquery"    % Version.googleCloudBigQuery
    val kamonCore           = "io.kamon"                   %% "kamon-core"              % Version.kamon
    val kamonAkkaHttp       = "io.kamon"                   %% "kamon-akka-http-2.5"     % "1.0.1"
    val kamonSystemMetrics  = "io.kamon"                   %% "kamon-system-metrics"    % Version.kamon
    val kamonPrometheus     = "io.kamon"                   %% "kamon-prometheus"        % Version.kamon
    val configAnnotation    = "com.wacai"                  %% "config-annotation"       % Version.wacaiConfigAnnotation
    val scalaLogging        = "com.typesafe.scala-logging" %% "scala-logging"           % Version.scalaLogging
    val slf4jApi            = "org.slf4j"                  % "slf4j-api"                % Version.slf4jApi
    val logback             = "ch.qos.logback"             % "logback-classic"          % Version.logback
    val logbackEncoder      = "net.logstash.logback"       % "logstash-logback-encoder" % Version.logbackEncoder
    val macroParadise       = "org.scalamacros"            % "paradise"                 % Version.macroParadise
    val scalactic           = "org.scalactic"              %% "scalactic"               % Version.scalatest
    val scalatest           = "org.scalatest"              %% "scalatest"               % Version.scalatest % "it,test"
    val scalacheck          = "org.scalacheck"             %% "scalacheck"              % Version.scalacheck % "it,test"
  }

// *****************************************************************************
// Settings
// *****************************************************************************
lazy val commonSettings = Def.settings(
  compileSettings
)

lazy val compileSettings = Def.settings(
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-encoding",
    "utf8",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code"
  ),
  scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
  scalacOptions in (Test, console) -= "-Ywarn-unused-import"
)

lazy val configAnnotationSettings: Seq[sbt.Setting[_]] =
  Seq(
    scalacOptions += "-Xmacro-settings:conf.output.dir=" + baseDirectory.value.getAbsolutePath + "/src/main/resources",
    addCompilerPlugin(library.macroParadise cross CrossVersion.full),
    libraryDependencies += library.configAnnotation
  )

lazy val dockerSettings: Seq[sbt.Setting[_]] =
  Seq(
    daemonUser.in(Docker) := "root",
    maintainer.in(Docker) := "TEST",
    dockerBaseImage := "java:8",
    dockerRepository := Some("dockerhub.io"),
    dockerUsername := sys.env.get("APP_PROJECT").orElse(Some("app"))
  )
