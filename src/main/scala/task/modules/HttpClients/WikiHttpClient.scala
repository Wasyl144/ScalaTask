package task

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
import scala.language.{implicitConversions, postfixOps}

object WikiHttpClient {
  type HttpClient = HttpRequest ⇒ Future[HttpResponse]
  
  def redirectOrResult(client: HttpClient, lang: String)(response: HttpResponse)(implicit materializer: Materializer): Future[HttpResponse] =
    response.status match {
      case StatusCodes.Found | StatusCodes.MovedPermanently | StatusCodes.SeeOther ⇒ {
        val newUri = s"https://$lang.wikipedia.org/api/rest_v1/page/summary/" + response.header[Location].get.uri
        // println("DEBUG:" + newUri)
        response.discardEntityBytes()

        client(HttpRequest(method = HttpMethods.GET, uri = newUri))
      }

      case _ ⇒ Future.successful(response)
    }

  def httpClientWithRedirect(client: HttpClient, lang: String)(implicit ec: ExecutionContext, materializer: Materializer): HttpClient = {
    lazy val redirectingClient: HttpClient =
      req ⇒ client(req).flatMap(redirectOrResult(redirectingClient, lang)) // recurse to support multiple redirects
    redirectingClient
  }
}