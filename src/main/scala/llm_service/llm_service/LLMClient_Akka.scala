package llm_service.llm_service

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import io.grpc.ManagedChannelBuilder
import llm_service.llm_service.{TextGeneratorGrpc, QueryRequest}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

import org.slf4j.LoggerFactory


object LLMClient_Akka {

  val log = LoggerFactory.getLogger(this.getClass)

  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    implicit val ec: ExecutionContextExecutor = system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(exception) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", exception)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      // Create the gRPC channel and stub
      val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
      val textGeneratorStub = TextGeneratorGrpc.stub(channel)

      // Create the routes using the gRPC stub
      val routes = new UserRoutes(textGeneratorStub)(context.system)
      startHttpServer(routes.userRoutes)(context.system)

      Behaviors.empty
    }

    // Start the ActorSystem
    ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }

}
