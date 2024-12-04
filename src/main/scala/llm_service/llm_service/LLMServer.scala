package llm_service.llm_service

import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import io.circe.syntax._
import io.circe.generic.auto._
import org.slf4j.LoggerFactory
import sttp.client3.HttpURLConnectionBackend
import sttp.client3._
import sttp.client3.circe._
import sttp.model.MediaType
import io.circe.parser                           // For parsing JSON responses




private class TextGeneratorImpl(apiGatewayUrl: String) extends TextGeneratorGrpc.TextGenerator {
  // HTTP client backend
  private val backend = HttpURLConnectionBackend()
  val log = LoggerFactory.getLogger(this.getClass)

  override def getMessage(req: QueryRequest): Future[QueryResponse] = {
    // Prepare the request payload
    val payload = Map("prompt" -> req.query)
    log.info("Server got query: " + req.query)

    // Make the HTTP POST request to the API Gateway
    val result = basicRequest
      .post(uri"$apiGatewayUrl")
      .body(payload.asJson.noSpaces) // Serialize to JSON
      .contentType  (MediaType.ApplicationJson.toString)
      .send(backend)


    log.info("got response back from Bedrock model")

    // Process the response synchronously
    val queryResponse = result.body match {
      case Right(successBody) =>
        // Parse the response JSON
        val generatedText = io.circe.parser.parse(successBody)
          .flatMap(_.hcursor.downField("generatedText").as[String])
          .getOrElse("Error: Unable to parse response")
        QueryResponse(response = s"The answer to your question is: $generatedText")
      case Left(errorBody) =>
        QueryResponse(response = s"Error: Unable to process request, received error: $errorBody")
    }

    // Wrap the result in a Future
    Future.successful(queryResponse)
  }
}

object LLMServer {
  val log = LoggerFactory.getLogger(this.getClass)
  val config =  ConfigFactory.load("application.conf")
  def main(args: Array[String]): Unit = {
    // Specify the port for the server to listen on
    val port = config.getInt("app.apiGateway.port")
    val apiGatewayUrl = config.getString("app.apiGateway.url")

    // Create a thread-safe execution context
    implicit val ec: ExecutionContext = ExecutionContext.global

    // Build the gRPC server
    val server: Server = ServerBuilder
      .forPort(port)
      .addService(TextGeneratorGrpc.bindService(new TextGeneratorImpl(apiGatewayUrl), ec)) // Pass the API Gateway URL
      .build()

    // Start the server
    server.start()
    log.info(s"Server started, listening on $port")

    // Add a shutdown hook to handle graceful termination
    sys.addShutdownHook {
      log.info("Shutting down server...")
      server.shutdown()
      log.info("Server shut down.")
    }

    // Keep the server running
    server.awaitTermination()
  }
}
