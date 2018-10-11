
val ver = "1.5.26"

val scalaVer = "2.12.7"

lazy val root = (project in file("."))
  .withId("my-fleet-girls")
  .settings(rootSettings)
  .aggregate(server, client, library)

val proxy = inputKey[Unit]("run proxy")
val prof = inputKey[Unit]("run profiler")
val runTester = inputKey[Unit]("run tester")
val runTesterEarth = taskKey[Unit]("run tester")

lazy val rootSettings = settings ++ disableAggregates ++ Seq(
  commands ++= Seq(start),
  proxy := (run in (client, Compile)).evaluated,
  assembly := {
    (assembly in update).value
    (assembly in client).value
  },
  run := (run in (server, Compile)).evaluated,
  stage := (stage in server).value,
  dist := (dist in server).value,
  scalikejdbcGen := (scalikejdbcGen in (server, Compile)).evaluated,
  prof := (run in (profiler, Compile)).evaluated,
  runTester := (run in (tester, Compile)).evaluated,
  runTesterEarth := runTester.toTask(" https://myfleet.moe").value
)

lazy val disableAggregates = Seq(
  assembly, stage, dist, scalikejdbcGen
).map {
  aggregate in _ := false
}

lazy val server = project
  .settings(settings)
  .dependsOn(library)
  .enablePlugins(PlayScala, ScalikejdbcPlugin, SbtWeb, BuildInfoPlugin)

lazy val client = project
  .settings(
    settings,
    assemblyJarName in assembly := "MyFleetGirls.jar"
  )
  .dependsOn(library)
  .enablePlugins(AssemblyPlugin, BuildInfoPlugin)

lazy val library = project
  .settings(settings)

lazy val update = project
  .settings(
    settings,
    assemblyJarName in assembly := "update.jar"
  )
  .enablePlugins(AssemblyPlugin)

lazy val profiler = project
  .settings(settings)
  .dependsOn(server)

lazy val tester = project
  .settings(settings)

lazy val settings = Seq(
  version := ver,
  scalaVersion := scalaVer,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-encoding", "UTF-8"),
  javacOptions ++= Seq("-encoding", "UTF-8"),
  updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error),
  updateOptions := updateOptions.value.withCachedResolution(true),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html")),
  homepage := Some(url("https://myfleet.moe")),
  fork in Test := true
)

def start = Command.command("start") { state =>
  val subState = Command.process("project server", state)
  Command.process("runProd", subState)
  state
}

