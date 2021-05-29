// package task.modules.httpRequests
// import scala.concurrent.Future
// import akka.http.scaladsl.model.HttpEntity
// import task.modules.httpClients.WikiHttpClient
// import akka.http.scaladsl.Http

// abstract class HttpRequest {
//   def initRequest(): HttpRequest
//   def makeRequest(request: HttpRequest): Future[Option[String]] = {
//     val simpleClient = Http().singleRequest(_: HttpRequest)

//     val redirectingClient =
//       WikiHttpClient.httpClientWithRedirect(simpleClient, lng)  
    
//     Thread.sleep(500)
//       val entityFuture: Future[Option[HttpEntity.Strict]] =
//       redirectingClient(request).flatMap(res => {
//         if (res.status == NotFound) {
//           None
//         }
//         res.entity.toStrict(3.seconds).map(Some(_))
//       })
//     entityFuture.map(_.map(_.data.utf8String))
//   }
// }
