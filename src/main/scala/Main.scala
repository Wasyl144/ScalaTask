import collection.JavaConverters._
import scala.io.Source

object Main extends App {

  /** Initialize values to use in prgram
    */

  // TODO: Maybe change the input file to get a path from console

  // Type your filename, this will take a file from the resources folder
  val FILE_NAME: String = "test.txt"

  // You can choose any language of transcription if you want
  val LANG: String = "lang=en&"

  // Initialize YouTube timedtext API URL with params
  val URL: String = "https://video.google.com/timedtext?" + LANG

  /** ReadFile codeblock is used to return set of id's
    * YouTube Videos
    */

  val readFile = {
    val file = Source.fromResource(FILE_NAME).getLines()
    val youTubeVideoRegex =
      raw"((?:https?:)?\/\/)?((?:www|m)\.)?((?:youtube\.com|youtu.be))(\/(?:[\w\-]+\?v=|embed\/|v\/)?)([\w\-]+)(\S+)".r
    file
      .filter((value) => {
        value match {
          case youTubeVideoRegex(_*) => {
            println("DEBUG: " + value + " its legit yt link")
            true
          }
          case _ => {
            println("DEBUG: Its not a YouTube legit link")
            false
          }
        }
      })
      .map((value) => {
        youTubeVideoRegex.replaceAllIn(value, m => m.group(5))
      })
      .toSet
  }

  /** Now im creating a HttpClient to get the YouTube Captions
    */

  println(readFile)

}
