package llm_service.llm_service
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import llm_service.llm_service.{QueryRequest, TextGeneratorGrpc}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit


class UserRoutes(textGeneratorStub: TextGeneratorGrpc.TextGeneratorStub)(implicit val system: akka.actor.typed.ActorSystem[_]) {

  implicit val ec: ExecutionContext = system.executionContext
  val log = LoggerFactory.getLogger(this.getClass)

  // gRPC interaction: call TextGenerator's GetMessage method
  private def getLLMResponse(query: String): Future[String] = {
    log.info("start getting response")
    val request = QueryRequest(query)
    textGeneratorStub.withDeadlineAfter(5, TimeUnit.MINUTES).getMessage(request).map(_.response) // Extract the response from QueryResponse
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