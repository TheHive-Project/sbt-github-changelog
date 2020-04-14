package org.thp.ghcl

import java.nio.file.{ Files, Path }

import scala.io.Source
import scala.util.matching.Regex

object Git {
  val remoteSection: Regex = "\\[remote \"([^\"]*)\"]".r
  val remoteUrl: Regex =
    "\\s*url\\s*=\\s*git@github.com:([^/]+)/([^.]+).git".r
  val newSection: Regex = "\\[.*".r

  def getGithubProject(folder: Path): (String, String) = {
    val gitConfigFile = folder.resolve(".git/config")
    val ownerProject =
      if (!Files.exists(gitConfigFile)) {
        None
      } else {
        val gitConfig = Source.fromFile(gitConfigFile.toFile)
        try {
          gitConfig
            .getLines()
            .foldLeft[(Option[String], Option[(String, String)])](None -> None) {
              case ((_, None), remoteSection(remote)) => Some(remote) -> None
              case ((Some(remote), None), remoteUrl(owner, project)) =>
                println(
                  s"Found github project $owner/$project in remote $remote"
                )
                None -> Some(owner -> project)
              case ((Some(_), None), newSection()) => None -> None
              case (other, _)                      => other
            }
            ._2
        } finally gitConfig.close()
      }
    ownerProject
      .getOrElse(
        throw new IllegalStateException(
          "The current directory is not a git repository with github remote"
        )
      )
  }
}
