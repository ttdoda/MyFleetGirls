
name := "MyFleetGirlsLibrary"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.6.1",
  "com.typesafe" % "config" % "1.3.3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html"))

homepage := Some(url("https://myfleet.moe"))
