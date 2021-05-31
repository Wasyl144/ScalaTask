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
import org.slf4j.LoggerFactory
import task.modules
import task.modules.filters.filter.NLPFilter
import task.modules.httpClients.Client
import task.modules.fileReader.FileReader

object Pipeline {

  val logger = LoggerFactory.getLogger(getClass().getSimpleName())

  def boot(config: MyConfig, path: String)(implicit
      materializer: Materializer,
      system: ActorSystem
  ) = {
    val videosFuture =
      Future.sequence(
        FileReader.videoIdsFromFile(path)
          .map(vidId => {
            try {
              Some(sendRequest(vidId, config.ytLink).map(_.map(response => {
                YTResponse(vidId, response)
              })))
            } catch {
              case e: Exception => {
                None
              }
            }
          }).flatten
          
      )
    val videos: Set[YTReady] = Await
      .result(videosFuture, Duration.Inf)
      .map(yt => {
        try {
          yt.map(ytRes => {
            Some(
            YTReady(
              ytRes.idVideo,
              ytRes.source,
              XML.loadString(ytRes.source).text.strip()
            )
          )
          }).flatten
          
        } catch {
          case e: Exception => {
            logger error(e.getMessage())
            None
          }
        }
      })
      .flatten

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
                      youtubeData.idVideo,
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

    logger info("Sending requests to Wikipedia")

    val wikipediaArticlesFuture = nouns.map(youtubeVideo => {
      val wikiArts = Future.sequence(
        youtubeVideo.list
          .map(noun => {
            try{
              Some(
                sendRequest(noun, config.wikiLink).map(_
                  .map(response => {
                    parse(response) match {
                      case Left(json)  => None
                      case Right(json) => Some(json)
                    }
                  })
                  .flatMap(jsonRes => {
                    jsonRes.flatMap(json => {
                      root.extract.string.getOption(json).flatMap(plainArt => {
                        root.extract_html.string.getOption(json).flatMap(article => {
                          root.content_urls.desktop.page.string.getOption(json).map(wikiLink => {
                            WikipediaArticles(
                      article,
                      plainArt,
                      wikiLink
                    )
                          })
                        })
                      })
                    })
                  })
              )
              )
            }
            catch {
              case e: Exception => {
                logger error(e.getMessage())
                None
              }
            }
          }).flatten
      )
      val result = Await.result(wikiArts, Duration.Inf)
      YouTubeVideo(
        youtubeVideo idVideo,
        youtubeVideo source,
        youtubeVideo.plainText,
        result.flatMap(_.toList).toList
      )
    })

    
    logger info ("Saving result to file... \n")
    os.write.over(os.pwd / "data.json", wikipediaArticlesFuture.asJson.noSpaces)
    logger info ("Done \n")
  }

  def sendRequest(word: String, url: String)(implicit
      materializer: Materializer,
      system: ActorSystem
  ): Future[Option[String]] = {

    // Init wikipedia RESTAPI URL
    val simpleClient = Http().singleRequest(_: HttpRequest)

    val redirectingClient =
      Client.httpClientWithRedirect(simpleClient, url)

    val request = HttpRequest(GET, uri = url + word)
    Thread.sleep(500)

    val entityFuture: Future[Option[HttpEntity.Strict]] =
      redirectingClient(request).flatMap(res => {
          
          res.entity.toStrict(3.seconds).map(Some(_))
        
      })
    entityFuture.map(_.map(_.data.utf8String))
  }
}
