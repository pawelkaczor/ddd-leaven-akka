import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.3.2"


val project = Project(
  id = "ddd-cqrs-leaven-akka",
  base = file("."),
  settings = Project.defaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := "ddd-cqrs-leaven-akka",
    version := "1.0",
    scalaVersion := "2.10.3",
    scalacOptions := Seq("-encoding", "utf8", "-feature", "-language:postfixOps"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "ch.qos.logback" % "logback-classic" % "1.1.1",
      "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test",
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.typesafe.akka" %% "akka-camel" % akkaVersion,
      "org.apache.activemq" % "activemq-camel" % "5.9.1",
      "org.apache.activemq" % "activeio-core" % "3.1.4",
      "org.apache.activemq" % "activemq-broker" % "5.9.1",
      "com.typesafe.slick" %% "slick" % "2.0.2",
      "com.h2database" % "h2" % "1.3.170",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "com.github.michaelpisula" %% "akka-persistence-inmemory" % "0.1-SNAPSHOT",
      "commons-io" % "commons-io" % "2.4" % "test"),
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    }
  )
) configs (MultiJvm)