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

  "videoIdsFromFile with unexistent file" should "throw FileNotFound exception" in {
    an[java.io.FileNotFoundException] should be thrownBy Pipeline
      .videoIdsFromFile("dewfwefwe")
  }

  "Article which not exists" should "return option: None" in {
    val responseFuture: Future[Option[String]] = Pipeline.sendWikipediaRequest("gjhgjhgjk", "en")
    val response = Await.result(responseFuture, Duration.Inf)
    
    assert(response.get == "")
  } 

}
