
name := "MyFleetGirlsServer"

val scalikeJdbcVer = "3.3.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % scalikeJdbcVer,
  "org.scalikejdbc" %% "scalikejdbc-config" % scalikeJdbcVer,
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.6.0-scalikejdbc-3.3",
  "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % scalikeJdbcVer,
  "com.github.nscala-time" %% "nscala-time" % "2.20.0",
  "mysql" % "mysql-connector-java" % "5.1.47",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.apache.abdera" % "abdera-parser" % "1.1.3",
  "net.sf.ehcache" % "ehcache" % "2.10.5",
  "org.flywaydb" %% "flyway-play" % "5.0.0",
  guice
)

routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(gzip)

includeFilter in (Assets, LessKeys.less) := "*.less"

LessKeys.compress := true

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "build"

TwirlKeys.templateImports ++= Seq("views._", "models.db._", "models.join._")

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html"))

homepage := Some(url("https://myfleet.terminal.moe"))

// docker
dockerRepository := Some("ponkotuy")
dockerUpdateLatest := true
