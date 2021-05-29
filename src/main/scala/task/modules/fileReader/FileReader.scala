package task.modules.fileReader

import scala.io.Source
import org.slf4j.LoggerFactory

object FileReader {

    val logger = LoggerFactory.getLogger(getClass().getSimpleName())
  /** ReadFile codeblock is used to return set of id's
    * YouTube Videos
    */

  def videoIdsFromFile(path: String): Set[String] = {
    val file = Source.fromFile(path).getLines()
    val youTubeVideoRegex =
      raw"^((?:https?:)?\/\/)?((?:www|m)\.)?((?:youtube\.com|youtu.be))(\/(?:[\w\-]+\?v=|embed\/|v\/)?)([\w\-]+)(\S+)?".r
    file
      .filter((value) => {
        value match {
          case youTubeVideoRegex(_*) => {
            logger info ("DEBUG: " + value + " its legit yt link")
            true
          }
          case _ => {
            logger warn ("DEBUG: Its not a YouTube legit link")
            false
          }
        }
      })
      .map((value) => {
        youTubeVideoRegex
          .replaceAllIn(value, matchedString => matchedString.group(5))
      })
      .toSet
  }
}
