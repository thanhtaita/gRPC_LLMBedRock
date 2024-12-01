package llm_service.llm_service

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}

private class TextGeneratorImpl extends TextGeneratorGrpc.TextGenerator{
  override def getMessage(req: QueryRequest): Future[QueryResponse] = {
    // create the response. Later this is where we make the call to the AWS Bedrock
    val reply = QueryResponse(response = "The answer for the question: " + req.query + " is together we make something great for humanity")
    // return a successful future
    Future.successful(reply)
  }
}


object LLMServer {
  def main(args: Array[String]): Unit = {
    // Specify the port for the server to listen on
    val port = 50051
    // Create a thread-safe execution context
    implicit val ec: ExecutionContext = ExecutionContext.global

    // Build the gRPC server
    val server: Server = ServerBuilder
      .forPort(port)
      .addService(TextGeneratorGrpc.bindService(new TextGeneratorImpl, ec)) // Bind the service implementation
      .build()

    // Start the server
    server.start()
    println(s"Server started, listening on $port")

    // Add a shutdown hook to handle graceful termination
    sys.addShutdownHook {
      println("Shutting down server...")
      server.shutdown()
      println("Server shut down.")
    }

    // Keep the server running
    server.awaitTermination()
  }
}
