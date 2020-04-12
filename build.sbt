lazy val `sbt-github-changelog` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-changelog",
    version := "0.1.0",
    organization := "org.thehive-project",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client" %% "core" % "2.0.7",
      "com.softwaremill.sttp.client" %% "play-json" % "2.0.7"
    )
  )
