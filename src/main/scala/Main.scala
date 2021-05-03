import collection.JavaConverters._
import scala.io.Source

import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import HttpMethods._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.alpakka.xml.scaladsl
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import akka.stream.alpakka.xml.scaladsl.XmlParsing
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await

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

  // Init akka.actors
  implicit val system = ActorSystem()

  // Init Akka streams
  implicit val materializer = ActorMaterializer()

  /** ReadFile codeblock is used to return set of id's
    * YouTube Videos
    */

  val readFile: Future[Set[String]] = Future {
    val file = Source.fromResource(FILE_NAME).getLines()
    val youTubeVideoRegex =
      raw"^((?:https?:)?\/\/)?((?:www|m)\.)?((?:youtube\.com|youtu.be))(\/(?:[\w\-]+\?v=|embed\/|v\/)?)([\w\-]+)(\S+)?".r
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
        youTubeVideoRegex
          .replaceAllIn(value, matchedString => matchedString.group(5))
      })
      .toSet
  }

  /*
   * Now im creating a HttpClient to get the YouTube Captions
   */

  def sendRequest(videoId: String): Future[String] = {
    val youTubeRequest = HttpRequest(GET, uri = URL + "v=" + videoId)
    println(URL + "v=" + videoId)
    val responseFuture = Http().singleRequest(youTubeRequest)
    val entityFuture: Future[HttpEntity.Strict] =
      responseFuture.flatMap(res => {
        res.entity.toStrict(3.seconds)
      })
    entityFuture.map(entity => entity.data.utf8String)
  }

  val parse = Flow[String]
    .map(ByteString(_))
    .via(XmlParsing.parser)
    .toMat(Sink.seq)(Keep.right)

  // TODO: Refactor this code now is only for test purposes

  // TODO: Create an Object to store data.

  var listOfDTOs: List[youTubeDTO] = List()

  // readFile.foreach(value => {
  //   val testObject: youTubeDTO = new youTubeDTO
  //   testObject.set_videoId(value)
  //   sendRequest(value).onComplete({
  //     case Success(value)     => {
  //       testObject.set_captionsDirty(value)
  //       println("////dddd////")
  //       println("passitoeie")
  //       println(testObject.get_captionsDirty())
  //       listOfDTOs.+:(testObject)
  //     }
  //     case Failure(exception) => println(exception)
  //   })
  //   println("////////////////////////////////////////////////////////////////")
  // })
  readFile.onComplete({
    case Success(file) => {
      file.foreach(value => {
        val testObject: youTubeDTO = new youTubeDTO
        testObject.set_videoId(value)
        sendRequest(value).onComplete({
          case Success(value) => {
            testObject.set_captionsDirty(value)
            println("////dddd////")
            println("passitoeie")
            println(testObject.get_captionsDirty())
            listOfDTOs.+:(testObject)
          }
          case Failure(exception) => println(exception)
        })
        println(
          "////////////////////////////////////////////////////////////////"
        )
      })
    }
    case Failure(exception) => println(exception)
  })


  // println(testObject.get_videoId)
  // println(testObject.get_captionsDirty())

}

class youTubeDTO {
  private var videoId: String = ""
  private var captionsDirty: String = ""
  private var captionsPlainText: String = ""

  // Getters and setters
  def set_videoId(id: String) {
    videoId = id
  }
  def get_videoId(): String = {
    videoId
  }
  def set_captionsDirty(captions: String) {
    captionsDirty = captions
  }
  def get_captionsDirty(): String = {
    captionsDirty
  }
  def set_captionsPlainText(captions: String) {
    captionsPlainText = captions
  }
  def get_captionsPlainText(): String = {
    captionsPlainText
  }
}
