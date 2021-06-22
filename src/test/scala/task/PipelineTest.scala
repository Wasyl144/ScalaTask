//package task
//
//import org.scalatest.flatspec.AnyFlatSpec
//import org.scalatest.matchers.should.Matchers
//
//import scala.language.postfixOps
//import akka.http.javadsl.model.HttpResponse
//import akka.stream.Materializer
//import akka.stream.ActorMaterializer
//import akka.actor.ActorSystem
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Await
//import scala.concurrent.Future
//import scala.concurrent.duration.Duration
//import com.typesafe.config.ConfigFactory
//
//class PipelineTest extends AnyFlatSpec with Matchers {
//  // Init akka.actors
//  implicit val system = ActorSystem("Task")
//
//  // Init Akka streams
//  implicit val materializer = ActorMaterializer()
//
//  val config = ConfigFactory.load()
//
//
//  "Article which  exists" should "return option" in {
//    val responseFuture: Future[Option[String]] = Pipeline.sendRequest("apple", config.getString("app.WIKIAPI_LINK"))
//    val response = Await.result(responseFuture, Duration.Inf)
//
//    assert(response.get != "")
//  }
//  "correct ytId with captions" should "work" in {
//    mock[getYT()]
//
//    nlpFilter = mock[NLP.filter]
//    wikipedia = mock[Wiki.reqest()]
//    clientYT = mock[]
//    when(getYT("poprawneID")).then(return {"{}"})
//    when(nlpFilter.filter(xd.lista_slow)).then(return xd.lista_slow)
//    when(wikiReq.get(_)).then("xd")
//
//    Pipeline.boot(MyConfig("",""))
//
//    verify(wikiReq.get(), len(xd.lista_slow))
//    verify(getYt("poprawneID")).once()
//    verify(NLP.filter()).once()
//
//  }
//  "YT id which exists" should "return option" in {
//    val responseFuture: Future[Option[String]] = Pipeline.sendRequest("NFHDHcs4BvQ", config.getString("app.YOUTUBEAPI_LINK"))
//    val response = Await.result(responseFuture, Duration.Inf)
//
//    assert(response.get != "")
//  }
//
//
//
//}
