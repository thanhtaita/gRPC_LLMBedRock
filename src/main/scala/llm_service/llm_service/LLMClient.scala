package llm_service.llm_service
import io.grpc.ManagedChannelBuilder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LLMClient {
  def main(args: Array[String]): Unit = {
    // Define the host and port for the gRPC server
    val host = "localhost"
    val port = 50051

    // Create a gRPC channel to communicate with the server
    val channel = ManagedChannelBuilder
      .forAddress(host, port)
      .usePlaintext() // Use plaintext (no SSL/TLS)
      .build()

    // Create an asynchronous stub for making calls
    val stub = TextGeneratorGrpc.stub(channel)

    // Create a request object
    val request = QueryRequest(query = "What is the meaning of life?")

    // Make the asynchronous call
    val responseFuture: Future[QueryResponse] = stub.getMessage(request)

    // Handle the response asynchronously
    responseFuture.onComplete {
      case scala.util.Success(reply) =>
        println(s"Received response: ${reply.response}")
      case scala.util.Failure(exception) =>
        println(s"RPC failed: ${exception.getMessage}")
    }

    // Keep the application alive until the response is received
    Thread.sleep(1000) // Adjust the duration as necessary

    // Shutdown the channel when done
    channel.shutdown()

  }
}
