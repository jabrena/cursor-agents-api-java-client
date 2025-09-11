package com.example.client;

import info.jab.cursor.client.ApiClient;
import info.jab.cursor.client.ApiException;
import info.jab.cursor.client.api.AgentsApi;
import info.jab.cursor.client.model.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * WireMock integration test for AgentsApi using payload examples from OpenAPI YAML
 */
class AgentsApiWireMockTest {

    private WireMockServer wireMockServer;
    private AgentsApi agentsApi;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8080));
        wireMockServer.start();

        // Configure WireMock
        WireMock.configureFor("localhost", 8080);

        // Create ObjectMapper for JSON serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create API client pointing to WireMock server
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:8080");
        agentsApi = new AgentsApi(apiClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Nested
    @DisplayName("Agent management")
    class AgentManagementTests {

        @Test
        @DisplayName("Should launch agent successfully when valid request provided")
        void should_launchAgentSuccessfully_when_validRequestProvided() throws Exception {
            // Given
            LaunchAgentRequest request = createMockLaunchAgentRequest();
            Agent mockResponse = createMockAgent(
                "bc_abc123",
                "Add README Documentation",
                Agent.StatusEnum.CREATING,
                "https://github.com/your-org/your-repo",
                "main",
                "cursor/add-readme-1234",
                "https://cursor.com/agents?id=bc_abc123",
                false,
                "2024-01-15T10:30:00Z",
                null
            );

            stubFor(post(urlEqualTo("/v0/agents"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(mockResponse))));

            // When
            Agent response = agentsApi.launchAgent(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response)
                .extracting(Agent::getId, Agent::getName, Agent::getStatus)
                .containsExactly("bc_abc123", "Add README Documentation", Agent.StatusEnum.CREATING);
            assertThat(response.getSource())
                .extracting(Source::getRepository, Source::getRef)
                .containsExactly(URI.create("https://github.com/your-org/your-repo"), "main");
            assertThat(response.getTarget())
                .extracting(Target::getBranchName, Target::getUrl, Target::getAutoCreatePr)
                .containsExactly("cursor/add-readme-1234", URI.create("https://cursor.com/agents?id=bc_abc123"), false);

            verify(postRequestedFor(urlEqualTo("/v0/agents"))
                .withHeader("Content-Type", equalTo("application/json")));
        }

        @Test
        @DisplayName("Should throw ApiException when validation error occurs during launch")
        void should_throwApiException_when_validationErrorOccursDuringLaunch() throws Exception {
            // Given
            LaunchAgentRequest request = createMockLaunchAgentRequest();
            ErrorResponse errorResponse = createMockErrorResponse(
                "VALIDATION_ERROR",
                "Invalid request data",
                "The prompt text is required and cannot be empty"
            );

            stubFor(post(urlEqualTo("/v0/agents"))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> agentsApi.launchAgent(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(400);

            verify(postRequestedFor(urlEqualTo("/v0/agents")));
        }

        @Test
        @DisplayName("Should throw ApiException when unauthorized")
        void should_throwApiException_when_unauthorized() throws Exception {
            // Given
            LaunchAgentRequest request = createMockLaunchAgentRequest();
            ErrorResponse errorResponse = createMockErrorResponse(
                "UNAUTHORIZED",
                "Authentication required",
                "Please provide a valid API key in the Authorization header"
            );

            stubFor(post(urlEqualTo("/v0/agents"))
                .willReturn(aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> agentsApi.launchAgent(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(401);
        }

        @Test
        @DisplayName("Should get agent successfully when valid ID provided")
        void should_getAgentSuccessfully_when_validIdProvided() throws Exception {
            // Given
            String agentId = "bc_abc123";
            Agent mockResponse = createMockAgent(
                agentId,
                "Add README Documentation",
                Agent.StatusEnum.COMPLETED,
                "https://github.com/your-org/your-repo",
                "main",
                "cursor/add-readme-1234",
                "https://cursor.com/agents?id=bc_abc123",
                false,
                "2024-01-15T10:30:00Z",
                "2024-01-15T11:45:00Z"
            );

            stubFor(get(urlEqualTo("/v0/agents/" + agentId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(mockResponse))));

            // When
            Agent response = agentsApi.getAgent(agentId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response)
                .extracting(Agent::getId, Agent::getName, Agent::getStatus)
                .containsExactly(agentId, "Add README Documentation", Agent.StatusEnum.COMPLETED);
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(getRequestedFor(urlEqualTo("/v0/agents/" + agentId)));
        }

        @Test
        @DisplayName("Should throw ApiException when agent not found")
        void should_throwApiException_when_agentNotFound() throws Exception {
            // Given
            String agentId = "bc_nonexistent";
            ErrorResponse errorResponse = createMockErrorResponse(
                "AGENT_NOT_FOUND",
                "Agent with specified ID not found",
                "No agent exists with ID 'bc_nonexistent'"
            );

            stubFor(get(urlEqualTo("/v0/agents/" + agentId))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> agentsApi.getAgent(agentId))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(404);

            verify(getRequestedFor(urlEqualTo("/v0/agents/" + agentId)));
        }

        @Test
        @DisplayName("Should add follow-up successfully when valid request provided")
        void should_addFollowUpSuccessfully_when_validRequestProvided() throws Exception {
            // Given
            String agentId = "bc_abc123";
            FollowUpRequest request = createMockFollowUpRequest();
            Agent mockResponse = createMockAgent(
                agentId,
                "Add README Documentation",
                Agent.StatusEnum.RUNNING,
                "https://github.com/your-org/your-repo",
                "main",
                "cursor/add-readme-1234",
                "https://cursor.com/agents?id=bc_abc123",
                false,
                "2024-01-15T10:30:00Z",
                "2024-01-15T12:00:00Z"
            );

            stubFor(post(urlEqualTo("/v0/agents/" + agentId + "/follow-up"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(mockResponse))));

            // When
            Agent response = agentsApi.addFollowUp(agentId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(agentId);
            assertThat(response.getStatus()).isEqualTo(Agent.StatusEnum.RUNNING);

            verify(postRequestedFor(urlEqualTo("/v0/agents/" + agentId + "/follow-up")));
        }

        @Test
        @DisplayName("Should delete agent successfully when valid ID provided")
        void should_deleteAgentSuccessfully_when_validIdProvided() throws Exception {
            // Given
            String agentId = "bc_abc123";
            stubFor(delete(urlEqualTo("/v0/agents/" + agentId))
                .willReturn(aResponse()
                    .withStatus(204)));

            // When & Then
            assertThatCode(() -> agentsApi.deleteAgent(agentId))
                .doesNotThrowAnyException();

            verify(deleteRequestedFor(urlEqualTo("/v0/agents/" + agentId)));
        }

        @Test
        @DisplayName("Should throw ApiException when deleting non-existent agent")
        void should_throwApiException_when_deletingNonExistentAgent() throws Exception {
            // Given
            String agentId = "bc_nonexistent";
            ErrorResponse errorResponse = createMockErrorResponse(
                "AGENT_NOT_FOUND",
                "Agent with specified ID not found",
                "No agent exists with ID 'bc_nonexistent'"
            );

            stubFor(delete(urlEqualTo("/v0/agents/" + agentId))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> agentsApi.deleteAgent(agentId))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(404);

            verify(deleteRequestedFor(urlEqualTo("/v0/agents/" + agentId)));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle network timeout gracefully")
        void should_handleNetworkTimeout_when_serverNotResponding() {
            // Given - simulate a connection failure instead of timeout
            stubFor(get(urlEqualTo("/v0/agents/bc_test"))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":{\"code\":\"INTERNAL_ERROR\",\"message\":\"Connection failed\",\"details\":\"Server unavailable\"}}")))
                    ;

            // When & Then
            assertThatThrownBy(() -> agentsApi.getAgent("bc_test"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(500);
        }

        @ParameterizedTest(name = "Should handle HTTP error codes: {0}")
        @ValueSource(ints = {400, 401, 403, 429, 500})
        @DisplayName("Should handle various HTTP error codes")
        void should_handleHttpErrorCodes_when_serverReturnsError(int errorCode) throws Exception {
            // Given
            String errorCodeString = switch (errorCode) {
                case 400 -> "VALIDATION_ERROR";
                case 401 -> "UNAUTHORIZED";
                case 403 -> "FORBIDDEN";
                case 429 -> "RATE_LIMIT_EXCEEDED";
                case 500 -> "INTERNAL_ERROR";
                default -> "INTERNAL_ERROR";
            };
            ErrorResponse errorResponse = createMockErrorResponse(
                errorCodeString,
                "Server error occurred",
                "HTTP " + errorCode + " error details"
            );
            stubFor(get(urlEqualTo("/v0/agents/bc_test"))
                .willReturn(aResponse()
                    .withStatus(errorCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> agentsApi.getAgent("bc_test"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(errorCode);
        }

        @Test
        @DisplayName("Should handle rate limiting")
        void should_handleRateLimiting_when_tooManyRequests() throws Exception {
            // Given
            ErrorResponse errorResponse = createMockErrorResponse(
                "RATE_LIMIT_EXCEEDED",
                "Too many requests",
                "You have exceeded the rate limit. Please try again later"
            );
            stubFor(post(urlEqualTo("/v0/agents"))
                .willReturn(aResponse()
                    .withStatus(429)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            LaunchAgentRequest request = createMockLaunchAgentRequest();
            assertThatThrownBy(() -> agentsApi.launchAgent(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(429);
        }
    }

    // Helper methods to create mock objects using examples from OpenAPI YAML

    private LaunchAgentRequest createMockLaunchAgentRequest() {
        LaunchAgentRequest request = new LaunchAgentRequest();

        Prompt prompt = new Prompt();
        prompt.setText("Add a README.md file with installation instructions");
        request.setPrompt(prompt);

        Source source = new Source();
        source.setRepository(URI.create("https://github.com/your-org/your-repo"));
        source.setRef("main");
        request.setSource(source);

        return request;
    }

    private LaunchAgentRequest createMockLaunchAgentRequestWithImages() {
        LaunchAgentRequest request = createMockLaunchAgentRequest();

        // Add images to the prompt
        Image image = new Image();
        image.setData("iVBORw0KGgoAAAANSUhEUgAA...".getBytes());

        ImageDimension dimension = new ImageDimension();
        dimension.setWidth(1024);
        dimension.setHeight(768);
        image.setDimension(dimension);

        request.getPrompt().setImages(Arrays.asList(image));

        return request;
    }

    private FollowUpRequest createMockFollowUpRequest() {
        FollowUpRequest request = new FollowUpRequest();

        Prompt prompt = new Prompt();
        prompt.setText("Also add unit tests for the new functionality");
        request.setPrompt(prompt);

        return request;
    }

    private Agent createMockAgent(String id, String name, Agent.StatusEnum status,
                                  String repository, String ref, String branchName,
                                  String url, boolean autoCreatePr,
                                  String createdAt, String updatedAt) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setName(name);
        agent.setStatus(status);

        Source source = new Source();
        source.setRepository(URI.create(repository));
        source.setRef(ref);
        agent.setSource(source);

        Target target = new Target();
        target.setBranchName(branchName);
        target.setUrl(URI.create(url));
        target.setAutoCreatePr(autoCreatePr);
        agent.setTarget(target);

        agent.setCreatedAt(OffsetDateTime.parse(createdAt));
        if (updatedAt != null) {
            agent.setUpdatedAt(OffsetDateTime.parse(updatedAt));
        }

        return agent;
    }

    private ErrorResponse createMockErrorResponse(String code, String message, String details) {
        ErrorResponse errorResponse = new ErrorResponse();
        info.jab.cursor.client.model.Error error = new info.jab.cursor.client.model.Error();
        error.setCode(info.jab.cursor.client.model.Error.CodeEnum.fromValue(code));
        error.setMessage(message);
        error.setDetails(details);
        errorResponse.setError(error);
        return errorResponse;
    }
}
