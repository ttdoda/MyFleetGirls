
libraryDependencies ++= Seq(
  "net.ceedubs" %% "ficus" % "1.1.2",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
  "org.json4s" %% "json4s-native" % "3.6.1",
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current
)

Keys.fork in Keys.run := true

javaOptions in Keys.run ++= Seq(
  "-agentlib:hprof=cpu=samples,depth=80,interval=2"
)
