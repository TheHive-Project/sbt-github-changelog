package org.thp.ghcl

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

case class Label(name: String) {
  override def toString: String = name
}

case class Issue(number: Int, title: String, date: TemporalAccessor, url: String, labels: Seq[String], isPR: Boolean) {
  def `type`(issueTypes: Seq[(String, Seq[String])]): Option[String] =
    issueTypes.collectFirst {
      case (issueType, typeLabels) if (isPR && typeLabels.contains("isPR")) || labels.exists(typeLabels.contains) =>
        issueType
    }
}

case class Milestone(title: String, date: TemporalAccessor, url: String, issues: Seq[Issue])

abstract class Renderer[T] {
  def write(t: T): String
}

object ChangeLog {
  val issueRenderer: Renderer[Issue] = (issue: Issue) =>
    s"- ${issue.title} [\\#${issue.number}](${issue.url})"
  def milestoneRenderer(
      issueRenderer: Renderer[Issue],
      issueTypes: Seq[(String, Seq[String])],
      defaultIssueType: String
  ): Renderer[Milestone] = {
    val issueTypeOrder = issueTypes.map(_._1).zipWithIndex.toMap
    (milestone: Milestone) =>
      milestone.issues
        .groupBy(_.`type`(issueTypes))
        .toSeq
        .sortBy(_._1.flatMap(issueTypeOrder.get).getOrElse(Int.MaxValue))
        .map {
          case (t, issues) =>
            s"""**${t.getOrElse(defaultIssueType)}:**
           |
           |${issues.map(issueRenderer.write).mkString("\n")}""".stripMargin
        }
        .mkString(
          s"## [${milestone.title}](${milestone.url}) (${DateTimeFormatter.ISO_LOCAL_DATE
            .format(milestone.date)})\n\n",
          "\n\n",
          "\n"
        )
  }

  def changeLogRenderer(
      milestoneRenderer: Renderer[Milestone]
  ): Renderer[Seq[Milestone]] =
    (milestones: Seq[Milestone]) =>
      s"""# Change Log
       |
       |${milestones
           .map(milestoneRenderer.write)
           .mkString("\n")}""".stripMargin

  def writeChangeLog(
      file: File,
      milestones: Seq[Milestone],
      changeLogRenderer: Renderer[Seq[Milestone]]
  ): File = {
    Files.write(
      file.toPath,
      changeLogRenderer.write(milestones).getBytes(StandardCharsets.UTF_8)
    )
    file
  }
}
