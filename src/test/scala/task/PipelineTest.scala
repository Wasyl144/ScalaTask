package task

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps
import akka.http.javadsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class PipelineTest extends AnyFlatSpec with Matchers {
  // Init akka.actors
  implicit val system = ActorSystem("Task")

  // Init Akka streams
  implicit val materializer = ActorMaterializer()

  /**
    * Proably im need to refactor http client
    * I cannot make tests cuz I dont know a status of response
    */

  // "Article which not exists" should "return option: None" in {
  //   val responseFuture: Future[Option[String]] = Pipeline.sendWikipediaRequest("gjhgjhgjk", "en")
  //   val response = Await.result(responseFuture, Duration.Inf)
    
  //   assert(response.get == "")
  // } 

  // "YT link which doesnt conatins subtitles" should "return option: None" in {
  //   val responseFuture: Future[String] = Pipeline.sendYouTubeRequest("gjhgjhgjk", "en")
  //   val response = Await.result(responseFuture, Duration.Inf)
  //   println(response)
  // } 

}
