package task

import collection.JavaConverters._
import scala.io.Source
import scala.xml._
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import HttpMethods._
import akka.{Done, NotUsed}

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.{ActorAttributes, ActorMaterializer, ClosedShape, IOResult, Materializer, Supervision, scaladsl}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, RunnableGraph, Sink, Zip}
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
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.optics.JsonPath._
import io.circe.generic.auto._

import scala.util.{Failure, Success, Try}
import scala.language.{implicitConversions, postfixOps}
import org.slf4j.LoggerFactory
import task.classes.Exceptions.PageNotFoundException
import task.modules
import task.modules.filters.filter.NLPFilter
import task.modules.httpClients.Client
import task.modules.fileReader.FileReader

import java.nio.file.Paths

object Pipeline {

  val logger = LoggerFactory.getLogger(getClass().getSimpleName())

  def boot(config: MyConfig, path: String)(implicit
                                           materializer: Materializer,
                                           system: ActorSystem
  ) = {

    // def getYoutubeVideoReadyForId(vidId: String): Future[Option[YTReady]] =
    //   for {
    //     ytResponse <- sendRequest(vidId, config.ytLink)
    //     parsedXML = ytResponse.map(XML.loadString(_).text.strip())
    //     // xd <- NLPFilter.filter(parsedXML)
    //   } yield parsedXML.map(parsed => YTReady(vidId, "", parsed))

    // Future
    //   .sequence(
    //     FileReader.videoIdsFromFile(path).map(getYoutubeVideoReadyForId(_))
    //   )
    //   .onComplete(println)
    // ()

    /**
      * TODO: Działa ale przez to po każdym rzuconym wyjątku stream bedzie kontynuowany
      *
      */

//    val decider: Supervision.Decider = {
//      case _: Exception =>
//        Supervision.Resume
//      case _            => Supervision.Stop
//    }

    logger.info("test")
    val youTubeVideoRegex =
      raw"^((?:https?:)?\/\/)?((?:www|m)\.)?((?:youtube\.com|youtu.be))(\/(?:[\w\-]+\?v=|embed\/|v\/)?)([\w\-]+)(\S+)?".r

    val graph = GraphDSL.create() {
      implicit builder =>
        import GraphDSL.Implicits._

        val filePath = Paths.get(path)
        val flow = scaladsl.Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 256,
          allowTruncation = true)
        val flowToUtf = Flow[ByteString].map(x => {
          x.utf8String
        })
        val fileSource = builder.add(scaladsl.FileIO.fromPath(filePath).via(flow).via(flowToUtf))
        val output = builder.add(Sink.foreach[Set[String]](println))

        val flowCheckYtlink = builder.add(Flow[String].filter {
          case youTubeVideoRegex(_*) => true
          case (_) => false
        }.map(validLink => youTubeVideoRegex.replaceAllIn(validLink, replacer => replacer.group(5))))



        val flowSendYoutubeRequest = builder.add(Flow[String].mapAsync(1) {
          sendRequest(_, config.ytLink)
        }.map {
          case Some(value) => XML.loadString(value).text
        }.withAttributes(supervisionStrategy(resumingDecider)))
        // Added strategy to continue stream https://doc.akka.io/docs/akka/2.5/stream/stream-error.html?language=scala#

        val flowNLPFilter = builder.add(Flow[String].mapAsync(1){
          NLPFilter.filter
        })

        val flowWikipediaRequest = builder.add(Flow[Set[String]].mapAsync(1) {
          sendRequest(_, config.wikiLink)
        })

        fileSource ~> flowCheckYtlink ~> flowSendYoutubeRequest ~> flowNLPFilter ~> output

        ClosedShape

    }

    RunnableGraph.fromGraph(graph).run()


    logger.info("test")

  }

  def sendRequest(prop: String, url: String)(implicit
                                             materializer: Materializer,
                                             system: ActorSystem
  ): Future[Option[String]] = {

    // Init wikipedia RESTAPI URL
    val simpleClient = Http().singleRequest(_: HttpRequest)

    val redirectingClient =
      Client.httpClientWithRedirect(simpleClient, url)

    val request = HttpRequest(GET, uri = url + prop)
    Thread.sleep(500)

    val entityFuture: Future[Option[HttpEntity.Strict]] =
      redirectingClient(request).flatMap(res => {

        res.entity.toStrict(3.seconds).map(Some(_))

      })
    entityFuture.map(_.map(_.data.utf8String))

  }
}
