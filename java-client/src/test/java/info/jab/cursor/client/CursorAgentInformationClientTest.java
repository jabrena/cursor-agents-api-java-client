package info.jab.cursor.client;

import info.jab.cursor.CursorAgentInformation;
import info.jab.cursor.CursorAgentInformationClient;
import info.jab.cursor.client.model.ListAgents200Response;
import info.jab.cursor.client.model.ListAgents200ResponseAgentsInner;
import info.jab.cursor.client.model.GetAgentConversation200Response;
import info.jab.cursor.client.model.GetAgentConversation200ResponseMessagesInner;

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
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CursorAgentInformationClient using WireMock to stub Cursor API responses.
 */
class CursorAgentInformationClientTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int WIREMOCK_PORT = 8080;
    private static final String WIREMOCK_BASE_URL = "http://localhost:" + WIREMOCK_PORT;

    private WireMockServer wireMockServer;
    private CursorAgentInformation client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", WIREMOCK_PORT);

        // Create ObjectMapper for JSON serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create client pointing to WireMock server
        client = new CursorAgentInformationClient(TEST_API_KEY, WIREMOCK_BASE_URL);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Client should be instantiable")
    void testClientInstantiation() {
        // Test default constructor
        CursorAgentInformation client1 = new CursorAgentInformationClient(TEST_API_KEY);
        assertNotNull(client1);

        // Test constructor with custom base URL
        CursorAgentInformation client2 = new CursorAgentInformationClient(TEST_API_KEY, WIREMOCK_BASE_URL);
        assertNotNull(client2);

        // Verify client properties
        CursorAgentInformationClient clientImpl = (CursorAgentInformationClient) client2;
        assertEquals(TEST_API_KEY, clientImpl.getApiKey());
        assertEquals(WIREMOCK_BASE_URL, clientImpl.getApiBaseUrl());
    }

    @Test
    @DisplayName("Client should validate input parameters")
    void testInputValidation() {
        // Test getStatus with null/empty agent ID
        assertThrows(IllegalArgumentException.class, () -> client.getStatus(null));
        assertThrows(IllegalArgumentException.class, () -> client.getStatus(""));
        assertThrows(IllegalArgumentException.class, () -> client.getStatus("   "));

        // Test getAgentConversation with null/empty agent ID
        assertThrows(IllegalArgumentException.class, () -> client.getAgentConversation(null));
        assertThrows(IllegalArgumentException.class, () -> client.getAgentConversation(""));
        assertThrows(IllegalArgumentException.class, () -> client.getAgentConversation("   "));
    }

    @Test
    @DisplayName("getAgents should return list of agents")
    void testGetAgents() throws Exception {
        // Create mock response
        ListAgents200Response mockResponse = new ListAgents200Response();

        ListAgents200ResponseAgentsInner agent1 = new ListAgents200ResponseAgentsInner();
        agent1.setId("agent-1");
        agent1.setName("Test Agent 1");
        agent1.setStatus(ListAgents200ResponseAgentsInner.StatusEnum.RUNNING);
        agent1.setCreatedAt(OffsetDateTime.now());

        ListAgents200ResponseAgentsInner agent2 = new ListAgents200ResponseAgentsInner();
        agent2.setId("agent-2");
        agent2.setName("Test Agent 2");
        agent2.setStatus(ListAgents200ResponseAgentsInner.StatusEnum.FINISHED);
        agent2.setCreatedAt(OffsetDateTime.now());

        mockResponse.setAgents(Arrays.asList(agent1, agent2));
        mockResponse.setNextCursor("next-cursor");

        // Stub the API call
        stubFor(get(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        ListAgents200Response response = client.getAgents(null, null);

        // Verify response
        assertNotNull(response);
        assertEquals(2, response.getAgents().size());
        assertEquals("agent-1", response.getAgents().get(0).getId());
        assertEquals("Test Agent 1", response.getAgents().get(0).getName());
        assertEquals(ListAgents200ResponseAgentsInner.StatusEnum.RUNNING, response.getAgents().get(0).getStatus());
        assertEquals("next-cursor", response.getNextCursor());

        // Verify the request was made correctly
        verify(getRequestedFor(urlEqualTo("/v0/agents"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY)));
    }

    @Test
    @DisplayName("getAgents with parameters should include query parameters")
    void testGetAgentsWithParameters() throws Exception {
        // Create mock response
        ListAgents200Response mockResponse = new ListAgents200Response();
        mockResponse.setAgents(Arrays.asList());

        // Stub the API call
        stubFor(get(urlPathEqualTo("/v0/agents"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("cursor", equalTo("test-cursor"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        ListAgents200Response response = client.getAgents(10, "test-cursor");

        // Verify response
        assertNotNull(response);

        // Verify the request was made correctly
        verify(getRequestedFor(urlPathEqualTo("/v0/agents"))
                .withQueryParam("limit", equalTo("10"))
                .withQueryParam("cursor", equalTo("test-cursor"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY)));
    }

    @Test
    @DisplayName("getStatus should return agent status")
    void testGetStatus() throws Exception {
        String agentId = "test-agent-id";

        // Create mock response
        ListAgents200ResponseAgentsInner mockAgent = new ListAgents200ResponseAgentsInner();
        mockAgent.setId(agentId);
        mockAgent.setName("Test Agent");
        mockAgent.setStatus(ListAgents200ResponseAgentsInner.StatusEnum.RUNNING);
        mockAgent.setSummary("Agent is running");
        mockAgent.setCreatedAt(OffsetDateTime.now());

        // Stub the API call
        stubFor(get(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockAgent))));

        // Execute the call
        ListAgents200ResponseAgentsInner response = client.getStatus(agentId);

        // Verify response
        assertNotNull(response);
        assertEquals(agentId, response.getId());
        assertEquals("Test Agent", response.getName());
        assertEquals(ListAgents200ResponseAgentsInner.StatusEnum.RUNNING, response.getStatus());
        assertEquals("Agent is running", response.getSummary());

        // Verify the request was made correctly
        verify(getRequestedFor(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY)));
    }

    @Test
    @DisplayName("getStatus should handle finished status")
    void testGetStatusFinished() throws Exception {
        String agentId = "finished-agent";

        // Create mock response with finished status
        ListAgents200ResponseAgentsInner mockAgent = new ListAgents200ResponseAgentsInner();
        mockAgent.setId(agentId);
        mockAgent.setName("Finished Agent");
        mockAgent.setStatus(ListAgents200ResponseAgentsInner.StatusEnum.FINISHED);
        mockAgent.setSummary("Agent completed successfully");
        mockAgent.setCreatedAt(OffsetDateTime.now());

        // Stub the API call
        stubFor(get(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockAgent))));

        // Execute the call
        ListAgents200ResponseAgentsInner response = client.getStatus(agentId);

        // Verify response
        assertNotNull(response);
        assertEquals(ListAgents200ResponseAgentsInner.StatusEnum.FINISHED, response.getStatus());
        assertEquals("Agent completed successfully", response.getSummary());
    }

    @Test
    @DisplayName("getAgentConversation should return conversation history")
    void testGetAgentConversation() throws Exception {
        String agentId = "test-agent-id";

        // Create mock response
        GetAgentConversation200Response mockResponse = new GetAgentConversation200Response();

        GetAgentConversation200ResponseMessagesInner message1 = new GetAgentConversation200ResponseMessagesInner();
        message1.setType(GetAgentConversation200ResponseMessagesInner.TypeEnum.USER_MESSAGE);
        message1.setText("Hello, please help me with this task.");

        GetAgentConversation200ResponseMessagesInner message2 = new GetAgentConversation200ResponseMessagesInner();
        message2.setType(GetAgentConversation200ResponseMessagesInner.TypeEnum.ASSISTANT_MESSAGE);
        message2.setText("I'll help you with that task. Let me start working on it.");

        mockResponse.setMessages(Arrays.asList(message1, message2));

        // Stub the API call
        stubFor(get(urlEqualTo("/v0/agents/" + agentId + "/conversation"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute the call
        GetAgentConversation200Response response = client.getAgentConversation(agentId);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getMessages());
        assertEquals(2, response.getMessages().size());
        assertEquals(GetAgentConversation200ResponseMessagesInner.TypeEnum.USER_MESSAGE, response.getMessages().get(0).getType());
        assertEquals("Hello, please help me with this task.", response.getMessages().get(0).getText());
        assertEquals(GetAgentConversation200ResponseMessagesInner.TypeEnum.ASSISTANT_MESSAGE, response.getMessages().get(1).getType());
        assertEquals("I'll help you with that task. Let me start working on it.", response.getMessages().get(1).getText());

        // Verify the request was made correctly
        verify(getRequestedFor(urlEqualTo("/v0/agents/" + agentId + "/conversation"))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY)));
    }

    @Test
    @DisplayName("API calls should handle 404 errors")
    void testHandles404Error() {
        String agentId = "non-existent-agent";

        // Stub the API call to return 404
        stubFor(get(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Agent not found\"}}")));

        // Execute the call and expect an exception
        Exception exception = assertThrows(Exception.class, () -> client.getStatus(agentId));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to get agent status"));
    }

    @Test
    @DisplayName("API calls should handle 401 unauthorized errors")
    void testHandles401Error() {
        String agentId = "test-agent";

        // Stub the API call to return 401
        stubFor(get(urlEqualTo("/v0/agents/" + agentId))
                .withHeader("Authorization", equalTo("Bearer " + TEST_API_KEY))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": {\"message\": \"Unauthorized\"}}")));

        // Execute the call and expect an exception
        Exception exception = assertThrows(Exception.class, () -> client.getStatus(agentId));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed to get agent status"));
    }
}
