# Bedrock Lambda Function and ChatGPT-like Application

### Code
[Bedrock Lambda GitHub Repository](https://github.com/thanhtaita/lambdaFunc_bedrock)

### General Idea
Create a ChatGPT-like application utilizing AWS resources such as **EC2**, **Lambda functions**, **API Gateway**, and **Bedrock**. The main purpose is not to optimize speed but to explore the use of diverse tools and frameworks, such as **gRPC**, **Akka HTTP**, and various AWS services.

### Technologies Used
- **SBT**: 1.10.6  
- **Scala**: 2.13.12  
- **Java**: 11  
- **ScalaPB**: 0.11.11 (for compiling Protocol Buffers)

### Project Flow
![Project Flow Diagram](https://github.com/user-attachments/assets/da8491ec-165c-4eab-8df8-9734c3e268a8)

1. **End User**: Sends a request to **Server A** with a query.
2. **Server A**:
   - Built on **Akka HTTP** and integrated with **gRPC** (as the gRPC client).
   - Forwards the query to **Server B** (a gRPC server).
3. **Server B**:
   - Defines the gRPC server and receives the query.
   - Sends an HTTP request to the **Lambda function** using API Gateway.
4. **Lambda Function**:
   - Receives the query and calls the Bedrock model, **Cohere**, to generate a response.
5. **Response Flow**:
   - The response is returned all the way back to the end user.

---

### Limitations
- Due to multiple connections, the response time is relatively long.
- On the first request, the system might time out because the connection is not yet established.
- Subsequent requests perform better as the connection remains active.

---

### Run Locally

#### 1. Akka gRPC Client and gRPC Server
To run the Akka gRPC client and server locally:

1. Clone the repository: git clone gRPC_LLMBedRock
2. Build and run the server: sbt clean -> sbt reload -> sbt update -> sbt combine -> run llm_service.llm_service.LLMServer
3. Open another terminal, direct to the project path sbt run llm_service.llm_service.LLMClient_Akka
4. Send a query using curl: curl "localhost:8080/llm?query=What%20is%20the%20largest%20animal?"

*Note: You may need to send the request multiple times to establish the connection and get a response.

5. Run test with sbt test (You might need to retry test execution to pass all test cases)

#### 2. Lambda Function with API Gateway
1. Clone the repository: git clone bedrock-lambda
2. Build the project: sbt clean -> sbt reload -> sbt update -> sbt combine
3. Run test: sbt test

*Note: The Lambda function must be deployed on AWS to fully work. You may need to retry running tests for all cases to pass successfully.







    
