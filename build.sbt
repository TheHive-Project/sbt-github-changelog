lazy val `sbt-github-changelog` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-changelog",
    version := "0.1.0",
    organization := "org.thehive-project",
    organizationName := "TheHive project",
    licenses += ("Apache-2.0", url(
      "http://www.apache.org/licenses/LICENSE-2.0"
    )),
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client" %% "core" % "2.0.7",
      "com.softwaremill.sttp.client" %% "play-json" % "2.0.7"
    ),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  )
