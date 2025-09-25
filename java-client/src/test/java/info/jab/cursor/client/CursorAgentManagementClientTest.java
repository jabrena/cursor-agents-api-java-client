package info.jab.cursor.client;

import info.jab.cursor.CursorAgentManagement;
import info.jab.cursor.CursorAgentManagementClient;
import info.jab.cursor.client.model.CreateAgent201Response;
import info.jab.cursor.client.model.CreateAgent201ResponseSource;
import info.jab.cursor.client.model.CreateAgent201ResponseTarget;
import info.jab.cursor.client.model.DeleteAgent200Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CursorAgentManagementClient using WireMock to stub Cursor API responses.
 */
class CursorAgentManagementClientTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int WIREMOCK_PORT = 8081;
    private static final String WIREMOCK_BASE_URL = "http://localhost:" + WIREMOCK_PORT;

    private WireMockServer wireMockServer;
    private CursorAgentManagement client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Start WireMock server on different port to avoid conflicts
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", WIREMOCK_PORT);

        // Create ObjectMapper for JSON serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create client pointing to WireMock server
        client = new CursorAgentManagementClient(TEST_API_KEY, WIREMOCK_BASE_URL);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Client should be instantiable")
    void testClientInstantiation() {
        // Test default constructor
        CursorAgentManagement client1 = new CursorAgentManagementClient(TEST_API_KEY);
        assertNotNull(client1);

        // Test constructor with custom base URL
        CursorAgentManagement client2 = new CursorAgentManagementClient(TEST_API_KEY, WIREMOCK_BASE_URL);
        assertNotNull(client2);

        // Verify client properties
        CursorAgentManagementClient clientImpl = (CursorAgentManagementClient) client2;
        assertEquals(TEST_API_KEY, clientImpl.getApiKey());
        assertEquals(WIREMOCK_BASE_URL, clientImpl.getApiBaseUrl());
    }

    @Test
    @DisplayName("Client should validate input parameters")
    void testInputValidation() {
        // Test launch with invalid parameters
        assertThrows(IllegalArgumentException.class, () -> client.launch(null, "model", "repo"));
        assertThrows(IllegalArgumentException.class, () -> client.launch("", "model", "repo"));
        assertThrows(IllegalArgumentException.class, () -> client.launch("prompt", null, "repo"));
        assertThrows(IllegalArgumentException.class, () -> client.launch("prompt", "", "repo"));
        assertThrows(IllegalArgumentException.class, () -> client.launch("prompt", "model", null));
        assertThrows(IllegalArgumentException.class, () -> client.launch("prompt", "model", ""));

        // Test followUp with invalid parameters
        assertThrows(IllegalArgumentException.class, () -> client.followUp(null, "prompt"));
        assertThrows(IllegalArgumentException.class, () -> client.followUp("", "prompt"));
        assertThrows(IllegalArgumentException.class, () -> client.followUp("agent-id", null));
        assertThrows(IllegalArgumentException.class, () -> client.followUp("agent-id", ""));

        // Test delete with invalid parameters
        assertThrows(IllegalArgumentException.class, () -> client.delete(null));
        assertThrows(IllegalArgumentException.class, () -> client.delete(""));
        assertThrows(IllegalArgumentException.class, () -> client.delete("   "));
    }

    @Test
    @DisplayName("launch should create and return new agent")
    void testLaunchAgent() throws Exception {
        String prompt = "Please help me fix the bug in my code";
        String model = "claude-3-5-sonnet-20241022";
        String repository = "https://github.com/user/repo";

        // Create mock response
        CreateAgent201Response mockResponse = new CreateAgent201Response();
        mockResponse.setId("agent-123");
        mockResponse.setName("Code Fix Agent");
        mockResponse.setStatus(CreateAgent201Response.StatusEnum.CREATING);
        mockResponse.setCreatedAt(OffsetDateTime.now());

        CreateAgent201ResponseSource source = new CreateAgent201ResponseSource();
        source.setRepository(repository);
        source.setRef("main");
        mockResponse.setSource(source);

        CreateAgent201ResponseTarget target = new CreateAgent201ResponseTarget();
        target.setAutoCreatePr(true);
        mockResponse.setTarget(target);

        // Stub the API call
        stubFor(post(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        CreateAgent201Response response = client.launch(prompt, model, repository);

        // Verify response
        assertNotNull(response);
        assertEquals("agent-123", response.getId());
        assertEquals("Code Fix Agent", response.getName());
        assertEquals(CreateAgent201Response.StatusEnum.CREATING, response.getStatus());
        assertNotNull(response.getCreatedAt());
        assertEquals(repository, response.getSource().getRepository().toString());
        assertEquals("main", response.getSource().getRef());
        assertTrue(response.getTarget().getAutoCreatePr());

        // Verify the request was made correctly
        verify(postRequestedFor(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("launch should handle different models")
    void testLaunchAgentWithDifferentModel() throws Exception {
        String prompt = "Refactor this code";
        String model = "gpt-4";
        String repository = "https://github.com/user/another-repo";

        // Create mock response
        CreateAgent201Response mockResponse = new CreateAgent201Response();
        mockResponse.setId("agent-456");
        mockResponse.setName("Refactor Agent");
        mockResponse.setStatus(CreateAgent201Response.StatusEnum.CREATING);
        mockResponse.setCreatedAt(OffsetDateTime.now());

        // Stub the API call
        stubFor(post(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        CreateAgent201Response response = client.launch(prompt, model, repository);

        // Verify response
        assertNotNull(response);
        assertEquals("agent-456", response.getId());
        assertEquals("Refactor Agent", response.getName());
        assertEquals(CreateAgent201Response.StatusEnum.CREATING, response.getStatus());
    }

    @Test
    @DisplayName("followUp should add follow-up to existing agent")
    void testFollowUpAgent() throws Exception {
        String agentId = "agent-123";
        String followUpPrompt = "Also please add unit tests";

        // Create mock response
        DeleteAgent200Response mockResponse = new DeleteAgent200Response();
        mockResponse.setId(agentId);

        // Stub the API call
        stubFor(post(urlEqualTo("/v0/agents/" + agentId + "/followup"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        DeleteAgent200Response response = client.followUp(agentId, followUpPrompt);

        // Verify response
        assertNotNull(response);
        assertEquals(agentId, response.getId());

        // Verify the request was made correctly
        verify(postRequestedFor(urlEqualTo("/v0/agents/" + agentId + "/followup"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("delete should remove agent")
    void testDeleteAgent() throws Exception {
        String agentId = "agent-to-delete";

        // Create mock response
        DeleteAgent200Response mockResponse = new DeleteAgent200Response();
        mockResponse.setId(agentId);

        // Stub the API call
        stubFor(delete(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        DeleteAgent200Response response = client.delete(agentId);

        // Verify response
        assertNotNull(response);
        assertEquals(agentId, response.getId());

        // Verify the request was made correctly
        verify(deleteRequestedFor(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY)));
    }

    @Test
    @DisplayName("launch should handle 400 bad request errors")
    void testLaunchHandles400Error() {
        String prompt = "Invalid prompt";
        String model = "invalid-model";
        String repository = "not-a-url";

        // Stub the API call to return 400
        stubFor(post(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Invalid request parameters\"}}")));

        // Execute the call and expect an exception
        Exception exception = assertThrows(Exception.class, () -> client.launch(prompt, model, repository));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to launch agent"));
    }

    @Test
    @DisplayName("followUp should handle 404 agent not found")
    void testFollowUpHandles404Error() {
        String agentId = "non-existent-agent";
        String prompt = "Follow up prompt";

        // Stub the API call to return 404
        stubFor(post(urlEqualTo("/v0/agents/" + agentId + "/followup"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Agent not found\"}}")));

        // Execute the call and expect an exception
        Exception exception = assertThrows(Exception.class, () -> client.followUp(agentId, prompt));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to add follow-up"));
    }

    @Test
    @DisplayName("delete should handle 404 agent not found")
    void testDeleteHandles404Error() {
        String agentId = "non-existent-agent";

        // Stub the API call to return 404
        stubFor(delete(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Agent not found\"}}")));

        // Execute the call and expect an exception
        Exception exception = assertThrows(Exception.class, () -> client.delete(agentId));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to delete agent"));
    }

    @Test
    @DisplayName("launch should handle 401 unauthorized errors")
    void testLaunchHandles401Error() {
        String prompt = "Test prompt";
        String model = "claude-3-5-sonnet-20241022";
        String repository = "https://github.com/user/repo";

        // Stub the API call to return 401
        stubFor(post(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Unauthorized\"}}")));

        // Execute the call and expect an exception
        Exception exception = assertThrows(Exception.class, () -> client.launch(prompt, model, repository));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to launch agent"));
    }
}
