package task

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
import akka.stream.scaladsl.Flow
import akka.util.ByteString
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
import scala.util.parsing.json.JSON
import io.circe._, io.circe.parser._
import io.circe.syntax._
import io.circe.optics.JsonPath._
import io.circe.generic.auto._

import scala.util.{Failure, Success, Try}
import akka.stream.Materializer
import scala.language.{implicitConversions, postfixOps}

object Pipeline {

  def boot(config: MyConfig, path: String)(implicit
      materializer: Materializer,
      system: ActorSystem
  ) = {
    val videosFuture =
      Future.sequence(
        videoIdsFromFile(path)
          .map(vidId => {
            try {
              Some(sendYouTubeRequest(vidId, config.lang).map(response => {
                YTResponse(vidId, response)
              }))
            } catch {
              case e: Exception => {
                None
              }
            }
          })
          .flatten
      )
    val videos: Set[YTReady] = Await
      .result(videosFuture, Duration.Inf)
      .map(ytRes => {
        try {
          Some(
            YTReady(
              ytRes.id,
              ytRes.source,
              XML.loadString(ytRes.source).text.strip()
            )
          )
        } catch {
          case e: Exception => {
            println(e.getMessage())
            None
          }
        }
      })
      .flatten

    // println("\n\n\n\n\n\n")
    // os.write(os.pwd / "data.json", videos.asJson.noSpaces)
    // println("\n\n\n\n\n\n")

    val nounsFuture: Future[Set[YTWithNouns]] =
      Future.sequence(
        videos
          .map(youtubeData => {
            try {
              Some(
                NLPFilter
                  .filter(youtubeData.plainText)
                  .map(setOfNouns => {
                    YTWithNouns(
                      youtubeData.id,
                      youtubeData.source,
                      youtubeData.plainText,
                      setOfNouns
                    )
                  })
              )
            } catch {
              case e: Exception => None
            }

          })
          .flatten
      )

    val nouns: Set[YTWithNouns] = Await.result(nounsFuture, Duration.Inf)

    val wikipediaArticlesFuture = nouns.map(youtubeVideo => {
      val wikiArts = Future.sequence(
        youtubeVideo.list
          .map(noun => {
            try {
              Some(
                sendWikipediaRequest(noun, config.lang)
                  .map(response => {
                    parse(response) match {
                      case Left(json)  => None
                      case Right(json) => Some(json)
                    }
                  })
                  .map(jsonRes => {
                    val json = jsonRes.get
                    val wikiArticlePlain =
                      root.extract.string.getOption(json) match {
                        case Some(value) => Some(value)
                        case None        => None
                      }
                    val wikiArticle =
                      root.extract_html.string.getOption(json) match {
                        case Some(value) => Some(value)
                        case None        => None
                      }
                    val wikiLink =
                      root.content_urls.desktop.page.string
                        .getOption(json) match {
                        case Some(value) => Some(value)
                        case None        => None
                      }
                    WikipediaArticles(
                      wikiArticle.get,
                      wikiArticlePlain.get,
                      wikiLink.get
                    )
                  })
              )
            } catch {
              case e: Exception => {
                println(e.getMessage())
                None
              }
            }
          })
          .flatten
      )
      val result = Await.result(wikiArts, Duration.Inf)
      YouTubeVideo(
        youtubeVideo id,
        youtubeVideo source,
        youtubeVideo.plainText,
        result.toList
      )
    })

    println("\n\n\n\n\n\n")
    os.write(os.pwd / "data.json", wikipediaArticlesFuture.asJson.noSpaces)
    println("\n\n\n\n\n\n")

  }

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

  def sendYouTubeRequest(videoId: String, lng: String)(implicit
      materializer: Materializer,
      system: ActorSystem
  ): Future[String] = {
    // You can choose any language of transcription if you want
    val LANG: String = s"lang=$lng&"

    // Initialize YouTube timedtext API URL with params
    val URL: String = "https://www.youtube.com/api/timedtext?" + LANG

    // Param neede to better text format
    val SUBTITLE_FORMAT = "&fmt=srv3"

    val request = HttpRequest(GET, uri = URL + "v=" + videoId + SUBTITLE_FORMAT)
    println(URL + "v=" + videoId + " - sending request")
    Thread.sleep(500)
    val responseFuture = Http().singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] =
      responseFuture.flatMap(res => {
        res.entity.toStrict(3.seconds)
      })
    entityFuture.map(entity => entity.data.utf8String)
  }

  def sendWikipediaRequest(word: String, lng: String)(implicit
      materializer: Materializer,
      system: ActorSystem
  ): Future[String] = {

    // Init wikipedia RESTAPI URL
    val URL: String =
      s"https://$lng.wikipedia.org/api/rest_v1/page/summary/"

    val simpleClient = Http().singleRequest(_: HttpRequest)

    val redirectingClient =
      WikiHttpClient.httpClientWithRedirect(simpleClient, lng)

    val request = HttpRequest(GET, uri = URL + word)
    Thread.sleep(500)

    val entityFuture: Future[HttpEntity.Strict] =
      redirectingClient(request).flatMap(res => {
        println(res.headers)
        res.entity.toStrict(3.seconds)
      })
    entityFuture.map(entity => entity.data.utf8String)
  }
}
