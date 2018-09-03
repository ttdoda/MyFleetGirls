import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.web.SbtWeb
import sbt.Keys._
import sbt._
import play._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin
import scalikejdbc.mapper.SbtKeys._

object MyFleetGirlsBuild extends Build {

  val ver = "1.5.24"

  val scalaVer = "2.11.11"

  lazy val root = Project(id = "my-fleet-girls", base = file("."), settings = rootSettings)
    .aggregate(server, client, library)

  val proxy = inputKey[Unit]("run proxy")
  val prof = inputKey[Unit]("run profiler")
  val runTester = inputKey[Unit]("run tester")
  val runTesterEarth = taskKey[Unit]("run tester")
  val downLib = taskKey[File]("download library")

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
    runTesterEarth := runTester.toTask(" https://myfleet.terminal.moe").value
  )

  lazy val disableAggregates = Seq(
    assembly, stage, dist, scalikejdbcGen
  ).map {
    aggregate in _ := false
  }

  lazy val server = project
    .dependsOn(library)
    .enablePlugins(sbt.PlayScala)
    .settings(
      scalaVersion := scalaVer,
      downLib := downLibTask.value,
      unmanagedJars in Compile := (unmanagedJars in Compile).dependsOn(downLib).value
    )
    .enablePlugins(SbtWeb, BuildInfoPlugin)

  lazy val client = project
    .settings(scalaVersion := scalaVer)
    .dependsOn(library)
    .enablePlugins(BuildInfoPlugin)

  lazy val library = project
    .settings(scalaVersion := scalaVer)

  lazy val update = project
    .settings(assemblyJarName in assembly := "update.jar")

  lazy val profiler = project
    .settings(scalaVersion := scalaVer)
    .dependsOn(server)

  lazy val tester = project
    .settings(scalaVersion := scalaVer)

  override lazy val settings = super.settings ++ Seq(
    version := ver,
    scalaVersion := scalaVer,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-encoding", "UTF-8"),
    javacOptions ++= Seq("-encoding", "UTF-8"),
    updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error),
    updateOptions := updateOptions.value.withCachedResolution(true),
    assemblyJarName in assembly := "MyFleetGirls.jar",
    incOptions := incOptions.value.withNameHashing(true),
    licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html")),
    homepage := Some(url("https://myfleet.terminal.moe")),
    fork in Test := true
  )

  def start = Command.command("start") { state =>
    val subState = Command.process("project server", state)
    Command.process("testProd", subState)
    state
  }

  def downLibTask = Def.task {
    val s = streams.value
    val libName = "ffdec_5.3.0_lib.jar"
    val libFile = unmanagedBase.value / libName
    if (!libFile.exists()) {
      IO.withTemporaryFile("ffdec-", ".tmp") { tmp =>
        val dlUrl = url(s"https://www.free-decompiler.com/flash/download/$libName")
        s.log.info(s"downloading $dlUrl ...")
        IO.download(dlUrl, tmp)
        IO.move(tmp, libFile)
      }
    }
    libFile
  }

}
