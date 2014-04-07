name := "ddd-cqrs-leaven-akka"

version := "1.0"

scalaVersion := "2.10.3"

val akkaVersion = "2.3.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.1",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"
  //"com.typesafe.akka" %% "akka-actor-tests" % akkaVersion % "test",
)
