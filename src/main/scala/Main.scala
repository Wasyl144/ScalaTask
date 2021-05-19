import collection.JavaConverters._
import scala.io.Source
import scala.xml._
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import HttpMethods._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
// import akka.stream.alpakka.xml.scaladsl
import akka.stream.scaladsl.Flow
import akka.util.ByteString
// import akka.stream.alpakka.xml.scaladsl.XmlParsing
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import edu.stanford.nlp.coref.data.CorefChain
import edu.stanford.nlp.ling._
import edu.stanford.nlp.ie.util._
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.semgraph._
import edu.stanford.nlp.trees._
import java.{util => ju}


case class YouTubeVideo(videoId: String, dirtySubtitles: String, plainSubtitles: String, wikipediaDetails: List[WikipediaArticles])
case class WikipediaArticles(article: String, title: String, link: String)

trait NounFilter {
  def filter (text: String): Set[String]
}

object NLPFilter extends NounFilter {
  def filter (text: String): Set[String] = {
    println("before \n")
    // println(text)

    val props: ju.Properties = new ju.Properties
    props.setProperty("annotators", "tokenize,ssplit,pos,parse")
    props.setProperty("coref.algorithm", "neural")

    val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

    val document: CoreDocument = new CoreDocument(text)

    println("document")

    pipeline.annotate(document)
    println("pipe")
    

    val nouns: Set[String] = document.sentences().asScala.flatMap(text => {
      text.tokens().asScala.filter(word => {
        word.tag().contains("NN") && text.posTags() != null
      })
    }).map(filteredNoun => filteredNoun.originalText()).toSet

    println("\n\n\n\n\n")

    nouns.foreach(value => {
      println(value)
    })

    nouns
  }
}

object Main extends App {

  /** Initialize values to use in prgram
    */

  // DEV: Maybe change the input file to get a path from console

  // Type your filename, this will take a file from the resources folder
  val FILE_NAME: String = "test.txt"

  // Init akka.actors
  implicit val system = ActorSystem()

  // Init Akka streams
  implicit val materializer = ActorMaterializer()

  /** ReadFile codeblock is used to return set of id's
    * YouTube Videos
    */

  
    // TESTING WIKIPEDIA

  // val readFile: Future[Set[String]] = Future {
  //   val file = Source.fromResource(FILE_NAME).getLines()
  //   val youTubeVideoRegex =
  //     raw"^((?:https?:)?\/\/)?((?:www|m)\.)?((?:youtube\.com|youtu.be))(\/(?:[\w\-]+\?v=|embed\/|v\/)?)([\w\-]+)(\S+)?".r
  //   file
  //     .filter((value) => {
  //       value match {
  //         case youTubeVideoRegex(_*) => {
  //           println("DEBUG: " + value + " its legit yt link")
  //           true
  //         }
  //         case _ => {
  //           println("DEBUG: Its not a YouTube legit link")
  //           false
  //         }
  //       }
  //     })
  //     .map((value) => {
  //       youTubeVideoRegex
  //         .replaceAllIn(value, matchedString => matchedString.group(5))
  //     })
  //     .toSet
  // }





  /*
   * Now im creating a HttpClient to get the YouTube Captions
   */

  def sendYouTubeRequest(videoId: String): Future[String] = {
    // You can choose any language of transcription if you want
    val LANG: String = "lang=en&"

    // Initialize YouTube timedtext API URL with params
    val URL: String = "https://www.youtube.com/api/timedtext?" + LANG

    // Param neede to better text format
    val SUBTITLE_FORMAT = "&fmt=srv3"

    val request = HttpRequest(GET, uri = URL + "v=" + videoId + SUBTITLE_FORMAT)
    println(URL + "v=" + videoId)
    val responseFuture = Http().singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] =
      responseFuture.flatMap(res => {
        println(res.headers)
        res.entity.toStrict(3.seconds)
      })
    entityFuture.map(entity => entity.data.utf8String)
  }


  def sendWikipediaRequest(word: String): Future[String] = {

    // You can choose a language
    val LANG: String = "en"

    // Init wikipedia RESTAPI URL
    val URL: String = "https://" + LANG + ".wikipedia.org/api/rest_v1/page/summary/"
    
    val QUERY: String = "?redirect=false"

    val request = HttpRequest(GET, uri = URL + word + QUERY)

    val responseFuture = Http().singleRequest(request)


    // TODO: Wikipedia api gives me a 301 status code
    // need to fix redirect
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(res => {
      println(res)
      res.entity.toStrict(3.seconds)
    })
    entityFuture.map(entity => entity.data.utf8String)

  }

  sendWikipediaRequest("gallon").onComplete({
    case Success(value) => println(value)
    case Failure(exception) => exception.getMessage()
  })



  // TODO: Refactor this code now is only for test purposes

  // TODO: Create an Object to store data.

  // DEV: Maybe create a microservice to store data, from requests


  /**
    * I'm catching a set of ids and im sending request one by one to youtube server
    *
    */

  /* TESTING WIKIPEDIA

  readFile.onComplete({
    case Success(file) => {
      file.foreach(videoId => {
        sendYouTubeRequest(videoId).onComplete({
          case Success(response) => {
            println(response)
            val nouns = NLPFilter.filter(XML.loadString(response).text)
            nouns.foreach(noun => {
              
            })
          }
          case Failure(exception) => println(exception.getMessage())
        })
        println(
          "////////////////////////////////////////////////////////////////"
        )
      })
    }
    case Failure(exception) => {
      println(exception.getMessage())
    }
  })

  */
}