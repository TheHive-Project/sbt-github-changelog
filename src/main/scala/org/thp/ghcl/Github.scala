package org.thp.ghcl

import java.io.File
import java.time.format.DateTimeFormatter

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, Reads }
import sttp.client._
import sttp.client.playJson._

import scala.io.Source

object Github {
  def readToken(tokenFile: File): String =
    if (tokenFile.exists()) {
      val f = Source.fromFile(tokenFile)
      try f.mkString.replaceAll("\\s", "")
      finally f.close()
    } else throw new IllegalStateException(s"File $tokenFile doesn't exist")

  implicit val labelReads: Reads[Label] =
    (JsPath \ "name").read[String].map(Label.apply)

  implicit val issueReads: Reads[Issue] =
    ((JsPath \ "node" \ "number").read[Int] and
    (JsPath \ "node" \ "title").read[String] and
    (JsPath \ "node" \ "url").read[String] and
    (JsPath \ "node" \ "labels" \ "nodes")
      .read[Seq[Label]]
      .map(_.map(_.name)))(Issue.apply _)

  implicit val milestoneReads: Reads[Milestone] =
    ((JsPath \ "title").read[String] and
    (JsPath \ "updatedAt")
      .read[String]
      .map(DateTimeFormatter.ISO_DATE_TIME.parse) and
    (JsPath \ "url").read[String] and
    (JsPath \ "issues" \ "edges").read[Seq[Issue]])(Milestone.apply _)

  val changeLogReads: Reads[Seq[Milestone]] = Reads[Seq[Milestone]] { js =>
    (js \ "data" \ "repository" \ "milestones" \ "nodes")
      .validate[Seq[Milestone]]
  }

  def getMilestones(
      token: String,
      ownerProject: (String, String),
      maxMilestones: Int,
      maxIssues: Int
  ): Seq[Milestone] = {
    val query =
      s"""
         |{
         |  repository(name: "${ownerProject._2}", owner: "${ownerProject._1}") {
         |    milestones(states: CLOSED, first: $maxMilestones, orderBy: {field: UPDATED_AT, direction: DESC}) {
         |      nodes {
         |        issues(first: $maxIssues, orderBy: {field: CREATED_AT, direction: DESC}) {
         |          edges {
         |            node {
         |              number
         |              title
         |              labels(first: 10) {
         |                nodes {
         |                  name
         |                }
         |              }
         |              url
         |            }
         |          }
         |        }
         |        title
         |        updatedAt
         |        url
         |      }
         |    }
         |  }
         |}
         |""".stripMargin

    implicit val backend: SttpBackend[Identity, Nothing, NothingT] =
      HttpURLConnectionBackend()

    val response = basicRequest.auth
      .bearer(token)
      .body(Json.obj("query" -> query))
      .post(uri"https://api.github.com/graphql")
      .response(asJson(changeLogReads, IsOption.otherIsNotOption))
      .send()
    response.body.fold(
      error =>
        throw new IllegalStateException(
          s"Got ${response.code.code} ${response.statusText}",
          error
        ),
      identity
    )
  }

}
