lazy val akkaHttpVersion = sys.props.getOrElse("akka-http.version", "10.7.0")
lazy val akkaVersion    = "2.10.0"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "CS441-hw3-nta3",

    // Specify the target for ScalaPB code generation
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
    ),

    // Add library dependencies
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "com.typesafe.akka" %% "akka-pki"                 % akkaVersion,

      "com.softwaremill.sttp.client3" %% "core" % "3.9.7",
      "com.softwaremill.sttp.client3" %% "circe" % "3.9.7",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser" % "0.14.9",

      "org.slf4j" % "slf4j-api" % "2.0.12",               // SLF4J 2.x for logging
      "ch.qos.logback" % "logback-classic" % "1.5.7",
      "com.typesafe" % "config" % "1.4.3",

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.19"        % Test
    ),

    fork := true,
    javaOptions += "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs match {
          case "MANIFEST.MF" :: Nil     => MergeStrategy.discard
          case "services" :: _          => MergeStrategy.concat
          case _                        => MergeStrategy.discard
        }
      case "reference.conf"            => MergeStrategy.concat
      case x if x.endsWith(".proto")   => MergeStrategy.rename
      case _                           => MergeStrategy.first
    }
  )
