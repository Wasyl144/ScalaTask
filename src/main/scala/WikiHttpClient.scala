import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.http.scaladsl.model.HttpMethods
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.model.StatusCodes.ClientError
object WikiHttpClient {
  val LANG: String = "en"
  type HttpClient = HttpRequest ⇒ Future[HttpResponse]

  def redirectOrResult(client: HttpClient)(response: HttpResponse)(implicit materializer: Materializer): Future[HttpResponse] =
    response.status match {
      case StatusCodes.Found | StatusCodes.MovedPermanently | StatusCodes.SeeOther ⇒ {
        val newUri = "https://" + LANG + ".wikipedia.org/api/rest_v1/page/summary/" + response.header[Location].get.uri
        println("DEBUG:" + newUri)
        response.discardEntityBytes()

        client(HttpRequest(method = HttpMethods.GET, uri = newUri))
      }

      case StatusCodes.NotFound => throw new Exception("Article not found")

      case _ ⇒ Future.successful(response)
    }

  def httpClientWithRedirect(client: HttpClient)(implicit ec: ExecutionContext, materializer: Materializer): HttpClient = {
    lazy val redirectingClient: HttpClient =
      req ⇒ client(req).flatMap(redirectOrResult(redirectingClient)) // recurse to support multiple redirects
    redirectingClient
  }
}