import com.typesafe.sbt.license.{LicenseInfo, DepModuleInfo}

// put this at the top of the file

name := "MyFleetGirls"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.littleshoot" % "littleproxy" % "1.1.2",
  "com.netaporter" %% "scala-uri" % "0.4.14",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.apache.httpcomponents" % "httpmime" % "4.5.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.25",
  "com.typesafe.akka" %% "akka-actor" % "2.4.6",
  "ch.qos.logback" % "logback-classic" % "1.1.11" % "runtime",
  "org.fusesource.jansi" % "jansi" % "1.16",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

mainClass in assembly := Some("com.ponkotuy.run.Main")

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "com.ponkotuy.build"

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html"))

homepage := Some(url("https://myfleet.iwmt.org"))

licenseOverrides := {
  case DepModuleInfo("org.json4s", _, _) | DepModuleInfo("org.apache.httpcomponents", _, _) | DepModuleInfo("com.google.guava", _, _) | DepModuleInfo("commons-codec", _, _) | DepModuleInfo("commons-collections", _, _) | DepModuleInfo("commons-lang", _, _) | DepModuleInfo("commons-logging", _, _) =>
    LicenseInfo(LicenseCategory.Apache, "The Apache Software Licnese, Version 2.0", "http://www.apache.org/licenses/LICENSE-2.0")
}
