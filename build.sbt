lazy val root = (project in file(".")).aggregate(rpc.js, rpc.jvm)

lazy val rpc = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "rpc",
    organization := "com.github.marcgrue",
    version := "0.1.0",
    scalaVersion := "2.13.6",
    scalacOptions ++=
      "-encoding" :: "UTF-8" ::
        "-unchecked" ::
        "-deprecation" ::
        "-explaintypes" ::
        "-feature" ::
        "-language:_" ::
        "-Xlint" ::
        "-Ywarn-value-discard" ::
        "-Ywarn-extra-implicit" ::
        "-Ywarn-unused" ::
        Nil,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.7.10",
      "io.suzaku" %%% "boopickle" % "1.3.3",
      "com.github.cornerman" %%% "sloth" % "0.3.0",
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0"
    ),
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % "2.6.14",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.14",
      "com.typesafe.akka" %% "akka-actor" % "2.6.14",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.14",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.14",
      "com.typesafe.akka" %% "akka-protobuf-v3" % "2.6.14",
      "com.typesafe.akka" %% "akka-http" % "10.2.4",
      "ch.megard" %% "akka-http-cors" % "1.1.1",
    )
  )
