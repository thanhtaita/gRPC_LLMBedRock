package llm_service.llm_service
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import sttp.client3.{HttpURLConnectionBackend, basicRequest}
import sttp.model.MediaType
import sttp.client3._
import io.circe.syntax._



class LLMTesting extends AnyFlatSpec with MockitoSugar{
  val config =  ConfigFactory.load("application.conf")
  val port = config.getInt("app.apiGateway.port")
  val apiGatewayUrl = config.getString("app.apiGateway.url")
  private val baseUrl = "http://localhost:8080/llm"
  private val backend = HttpURLConnectionBackend()


  "API Gateway" should "should return successful response" in {
    val payload = Map("prompt" -> "What animal is the largest?")
    // Make the HTTP POST request to the API Gateway
    val result = basicRequest
      .post(uri"$apiGatewayUrl")
      .body(payload.asJson.noSpaces) // Serialize to JSON
      .contentType  (MediaType.ApplicationJson.toString)
      .send(backend)

    // Process the response synchronously
    val queryResponse = result.body match {
      case Right(successBody) =>
        // Parse the response JSON
        val generatedText = io.circe.parser.parse(successBody)
          .flatMap(_.hcursor.downField("generatedText").as[String])
          .getOrElse("Error: Unable to parse response")
        assert(generatedText != "")
        assert(generatedText != "Query is empty")

      case Left(errorBody) =>
       assert(false)
    }
  }

  "API Gateway" should "should return empty response" in {
    val payload = Map("prompt" -> "")
    // Make the HTTP POST request to the API Gateway
    val result = basicRequest
      .post(uri"$apiGatewayUrl")
      .body(payload.asJson.noSpaces) // Serialize to JSON
      .contentType  (MediaType.ApplicationJson.toString)
      .send(backend)

    // Process the response synchronously
    val queryResponse = result.body match {
      case Right(successBody) =>
        // Parse the response JSON
        val generatedText = io.circe.parser.parse(successBody)
          .flatMap(_.hcursor.downField("generatedText").as[String])
          .getOrElse("Error: Unable to parse response")
        assert(generatedText == "Query is empty")

      case Left(errorBody) =>
        fail(s"Request failed with error: $errorBody")
    }
  }

  "Akka gRPC Client" should "should return response successfully" in  {
    val query = "What animal is the largest?"
    val apiUrl = uri"$baseUrl?query=${java.net.URLEncoder.encode(query, "UTF-8")}"

    // Make the HTTP GET request
    val result = basicRequest
      .get(apiUrl)
      .contentType(MediaType.ApplicationJson.toString)
      .send(backend)

    // Process the response
    result.body match {
      case Right(responseBody) =>
        assert(responseBody.startsWith("The answer to your question is: "))
        assert(responseBody != "The answer to your question is: Query is empty")
        val generatedText = responseBody.stripPrefix("The answer to your question is: ").trim
        assert(generatedText.nonEmpty, "Generated text should not be empty")

      case Left(errorBody) =>
        fail(s"Request failed with error: $errorBody")
    }
  }

  "API Gateway" should "return 'The answer to your question is: Query is empty' for an empty query" in {
    val query = "" // Empty query
    val apiUrl = uri"$baseUrl?query=${java.net.URLEncoder.encode(query, "UTF-8")}"

    // Make the HTTP GET request
    val result = basicRequest
      .get(apiUrl)
      .contentType(MediaType.ApplicationJson.toString)
      .send(backend)

    // Process the response
    result.body match {
      case Right(responseBody) =>
        assert(responseBody == "The answer to your question is: Query is empty")

      case Left(errorBody) =>
        fail(s"Request failed with error: $errorBody")
    }
  }
}
