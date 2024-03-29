package org.thp.ghcl

import sbt.Keys._
import sbt._

object SbtGithubChangelogPlugin extends AutoPlugin {
  final object autoImport {
    lazy val token = taskKey[String]("Get the Github authentication token")
    lazy val tokenFile =
      settingKey[File]("File containing the Github authentication token")
    lazy val changelogFile = settingKey[File]("Changelog file location")
    lazy val githubProject =
      taskKey[(String, String)](
        "Owner and project name (\"TheHive-Project\" -> \"sbt-github-changelog\" for example)"
      )
    lazy val issueFilter = settingKey[Issue => Boolean]("Issue filter")
    lazy val milestoneFilter = settingKey[Milestone => Boolean]("Milestone filter")
    lazy val issueRenderer =
      settingKey[Renderer[Issue]]("Describe how an issue is rendered")
    lazy val milestoneRenderer =
      settingKey[Renderer[Milestone]]("Describe how a milestone is rendered")
    lazy val issueTypes = settingKey[Seq[(String, Seq[String])]](
      "List of issue types with associated labels"
    )
    lazy val defaultIssueType = settingKey[String]("Default issue type")
    lazy val changeLogRenderer = settingKey[Renderer[Seq[Milestone]]](
      "Describe how a full change log is rendered"
    )
    lazy val changeLog = taskKey[File]("Generate the change log")
    lazy val maxMilestones =
      settingKey[Int]("Max number of milestone in change log")
    lazy val maxIssues =
      settingKey[Int]("Max number of issues in each milestone")
  }

  import autoImport._

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    tokenFile := file(s"${System.getProperty("user.home")}/.github/token"),
    token := Github.readToken(tokenFile.value),
    changelogFile := file("CHANGELOG.md"),
    githubProject := Git.getGithubProject(baseDirectory.value.toPath),
    issueFilter := (_ => true),
    milestoneFilter := (_ => true),
    issueRenderer := ChangeLog.issueRenderer,
    issueTypes := Seq(
      "Implemented enhancements" -> Seq("enhancement", "feature request"),
      "Fixed bugs" -> Seq("bug"),
      "Pull requests" -> Seq("isPR")
    ),
    defaultIssueType := "Closed issues",
    milestoneRenderer := ChangeLog
      .milestoneRenderer(
        issueRenderer.value,
        issueTypes.value,
        defaultIssueType.value
      ),
    changeLogRenderer := ChangeLog.changeLogRenderer(milestoneRenderer.value),
    changeLog := ChangeLog.writeChangeLog(
      changelogFile.value,
      Github
        .getMilestones(
          token.value,
          githubProject.value,
          maxMilestones.value,
          maxIssues.value
        )
        .collect {
          case milestone if milestoneFilter.value(milestone) =>
            milestone.copy(issues = milestone.issues.filter(issueFilter.value))
        },
      changeLogRenderer.value
    ),
    maxMilestones := 100,
    maxIssues := 100
  )
}
