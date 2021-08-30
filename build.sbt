lazy val `sbt-github-changelog` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-changelog",
    version := "0.4.0",
    organization := "org.thehive-project",
    organizationName := "TheHive project",
    organizationHomepage := Some(url("http://thehive-project.org/")),
    homepage := Some(url("https://github.com/TheHive-Project/sbt-github-changelog")),
    description := "SBT plugin to generate change log file from github milestones",
    versionScheme := Some("semver-spec"),
    scmInfo := Some(
        ScmInfo(
          url("https://github.com/TheHive-Project/sbt-github-changelog"),
          "scm:git@github.com:TheHive-Project/sbt-github-changelog.git"
        )
      ),
    developers := List(
        Developer(
          id = "toom",
          name = "Thomas",
          email = "thomas@thehive-project.org",
          url = url("https://github.com/To-om")
        )
      ),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    libraryDependencies ++= Seq(
        "com.softwaremill.sttp.client" %% "core"      % "2.2.9",
        "com.softwaremill.sttp.client" %% "play-json" % "2.2.9"
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
    pomIncludeRepository := { _ => false },
    publishTo := sonatypePublishToBundle.value,
    publishMavenStyle := true
  )
