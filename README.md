# sbt-github-changelog
sbt-github-changelog is a simple sbt plugins that generates a change log file from Github milestone content.

## How to use
This plugin requires sbt 1.3+

Add the following line in your `project/plugins.sbt` file:
```sbt
addSbtPlugin("org.thehive-project" % "sbt-github-changelog" % "0.1.0")
```

You need to [generate a Github authentication token](https://github.com/settings/tokens/new?description=sbt-github-changelog) and store it in the file `~/.github/token`

Under a Github project repository, run the task `changeLog`

## Configuration
The Github authentication token can be stored in sbt file (but don't commit it) using the setting `token`.

By default, sbt-github-changelog reads the authentication token from the file `tokenFile` (which is `$HOME/.github/token` by default).

The task `changeLog` generates the change log file in `changeLogFile` (`CHANGELOG.md` by default).

The plugin extracts the project name and owner from git remote configuration (`.git/config`). These values can be set using the setting `githubProject`:
```sbt
githubProject := "TheHive-Project" -> "sbt-github-changelog"
```

Inside a milestone, issues are grouped according to its type. You can configure types by defining labels associated with:
```sbt
issueTypes := Seq(
  "Fixed bugs" -> Seq("bug"),
  "Implemented enhancements" -> Seq("enhancement", "feature request")
)
```

If no type is found, the `defaultIssueType` is used.

This plugin doesn't support pagination when it retrieves data from Github. This means that there is a maximum number of milestones (`maxMilestones`), and a maximum number of issues in milestone (`maxIssues`). Both are set to 100 by default.
 
## Custom rendering

You can customize renderring of issue, milestone and whole change log:
```scala
import org.thp.ghcl._
issueRenderer := ((issue: Issue) ⇒ s"- ${issue.title} [\\#${issue.number}](${issue.url})"),
    milestoneRenderer := { (milestone: Milestone) ⇒
      val date = DateTimeFormatter
        .ISO_LOCAL_DATE
        .format(milestone.date)
      milestone
        .issues
        .groupBy(_.`type`(issueTypes.value))
        .map {
          case (t, issues) ⇒
            s"""**${t.getOrElse(defaultIssueType.value)}:**
               |
               |${issues.map(issueRenderer.value.write).mkString("\n")}""".stripMargin
        }
        .mkString(
          s"## [${milestone.title}](${milestone.url}) ($date)\n\n",
          "\n\n",
          "\n"
        )
    },
    changeLogRenderer := { (milestones: Seq[Milestone]) ⇒
      "# Change Log\n\n" + milestones
        .map(milestoneRenderer.value.write)
        .mkString("\n")
    }
```