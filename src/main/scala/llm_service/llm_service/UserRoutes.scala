package llm_service.llm_service
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import llm_service.llm_service.{QueryRequest, TextGeneratorGrpc}

import scala.concurrent.Future
import scala.concurrent.duration._

class UserRoutes(textGeneratorStub: TextGeneratorGrpc.TextGeneratorStub)(implicit val system: akka.actor.typed.ActorSystem[_]) {

  implicit val timeout: Timeout = 5.seconds
  implicit val ec = system.executionContext

  // gRPC interaction: call TextGenerator's GetMessage method
  private def getLLMResponse(query: String): Future[String] = {
    val request = QueryRequest(query)
    textGeneratorStub.getMessage(request).map(_.response) // Extract the response from QueryResponse
  }

  val userRoutes: Route =
    path("llm") {
      get {
        parameter("query") { query =>
          onSuccess(getLLMResponse(query)) { response =>
            complete(response) // Send the gRPC response as HTTP response
          }
        }
      }
    }
}