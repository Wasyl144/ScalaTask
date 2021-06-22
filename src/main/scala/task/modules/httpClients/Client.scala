package task.modules.httpClients

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
import scala.util.matching.Regex
import task.classes.Exceptions.PageNotFoundException

object Client {
  type HttpClient = HttpRequest ⇒ Future[HttpResponse]

  def redirectOrResult(client: HttpClient, url: String)(
      response: HttpResponse
  )(implicit materializer: Materializer): Future[HttpResponse] =
    response.status match {
      case StatusCodes.Found | StatusCodes.MovedPermanently |
          StatusCodes.SeeOther ⇒ {
        val httpRegEx: Regex =
          raw"(https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]+\.[^\s]{2,}|www\.[a-zA-Z0-9]+\.[^\s]{2,})".r
        val newUri = httpRegEx.findFirstIn(
          response.header[Location].get.uri.toString()
        ) match {
          case Some(value) => value
          case None        => url + response.header[Location].get.uri.toString()
        }
        response.discardEntityBytes()

        client(HttpRequest(method = HttpMethods.GET, uri = newUri))
      }
      case StatusCodes.NotFound => Future.failed(PageNotFoundException())

      case _ ⇒ Future.successful(response)
    }

  def httpClientWithRedirect(client: HttpClient, url: String)(implicit
      ec: ExecutionContext,
      materializer: Materializer
  ): HttpClient = {
    lazy val redirectingClient: HttpClient =
      req ⇒
        client(req).flatMap(
          redirectOrResult(redirectingClient, url)
        )
    redirectingClient
  }
}
