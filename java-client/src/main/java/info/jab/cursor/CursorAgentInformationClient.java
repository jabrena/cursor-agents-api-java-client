package info.jab.cursor;

import info.jab.cursor.client.ApiClient;
import info.jab.cursor.client.api.DefaultApi;
import info.jab.cursor.client.model.ListAgents200ResponseAgentsInner;
import info.jab.cursor.client.model.ListAgents200Response;
import info.jab.cursor.client.model.GetAgentConversation200Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Client implementation for Cursor Agent Information operations.
 * This class provides access to agent information APIs including status checks,
 * agent listing, and conversation history retrieval.
 */
public class CursorAgentInformationClient implements CursorAgentInformation {

    private static final String DEFAULT_API_BASE_URL = "https://api.cursor.com";

    private final String apiKey;
    private final String apiBaseUrl;
    private final DefaultApi defaultApi;

    /**
     * Creates a new CursorAgentInformationClient with the specified API key.
     * Uses the default API base URL.
     *
     * @param apiKey The API key for authentication with Cursor API
     */
    public CursorAgentInformationClient(String apiKey) {
        this(apiKey, DEFAULT_API_BASE_URL);
    }

    /**
     * Creates a new CursorAgentInformationClient with the specified API key and base URL.
     *
     * @param apiKey The API key for authentication with Cursor API
     * @param apiBaseUrl The base URL for the Cursor API
     */
    public CursorAgentInformationClient(String apiKey, String apiBaseUrl) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;

        // Initialize API client
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(apiBaseUrl);

        this.defaultApi = new DefaultApi(apiClient);
    }

    /**
     * Gets a list of agents with optional pagination.
     *
     * @param limit Maximum number of agents to return (optional, can be null)
     * @param cursor Pagination cursor for retrieving next page (optional, can be null)
     * @return ListAgents200Response containing the list of agents
     * @throws Exception if the operation fails
     */
    @Override
    public ListAgents200Response getAgents(Integer limit, String cursor) throws Exception {
        // Prepare authentication headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        try {
            return defaultApi.listAgents(limit, cursor, headers);
        } catch (Exception e) {
            throw new Exception("Failed to get agents list: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the current status of an agent.
     * This method performs a single status check.
     *
     * @param agentId The ID of the agent to check
     * @return The current ListAgents200ResponseAgentsInner instance with updated status
     * @throws Exception if status check fails
     */
    @Override
    public ListAgents200ResponseAgentsInner getStatus(String agentId) throws Exception {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent ID cannot be null or empty");
        }

        // Prepare authentication headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        try {
            // Get current agent status - single API call
            return defaultApi.getAgent(agentId, headers);
        } catch (Exception statusException) {
            // If status parsing fails due to unknown enum value, try to handle gracefully
            if (statusException.getMessage() != null && statusException.getMessage().contains("Unexpected value")) {
                // For now, re-throw the exception. The calling layer can handle unknown statuses
                throw new Exception("Agent status contains unknown value: " + statusException.getMessage(), statusException);
            } else {
                throw new Exception("Failed to get agent status: " + statusException.getMessage(), statusException);
            }
        }
    }

    /**
     * Gets the conversation history for a specific agent.
     *
     * @param agentId The ID of the agent to retrieve conversation for
     * @return GetAgentConversation200Response containing the agent's conversation history
     * @throws Exception if the operation fails
     */
    @Override
    public GetAgentConversation200Response getAgentConversation(String agentId) throws Exception {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent ID cannot be null or empty");
        }

        // Prepare authentication headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        try {
            return defaultApi.getAgentConversation(agentId, headers);
        } catch (Exception e) {
            throw new Exception("Failed to get agent conversation: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the API key used by this client.
     *
     * @return The API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the API base URL used by this client.
     *
     * @return The API base URL
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    /**
     * Checks if the given status represents a terminal state (completed, failed, etc.).
     */
    private boolean isTerminalStatus(ListAgents200ResponseAgentsInner.StatusEnum status) {
        if (status == null) {
            return false;
        }

        // Terminal statuses that indicate the agent has finished
        return status == ListAgents200ResponseAgentsInner.StatusEnum.FINISHED ||
               status == ListAgents200ResponseAgentsInner.StatusEnum.ERROR ||
               status == ListAgents200ResponseAgentsInner.StatusEnum.EXPIRED;
    }

    /**
     * Checks if the given status string represents a terminal state.
     * This handles both known enum values and API-specific statuses like "FINISHED".
     */
    private boolean isTerminalStatusString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        String upperStatus = status.toUpperCase().trim();

        // Known terminal statuses
        return "COMPLETED".equals(upperStatus) ||
               "FAILED".equals(upperStatus) ||
               "CANCELLED".equals(upperStatus) ||
               "FINISHED".equals(upperStatus) ||
               "EXPIRED".equals(upperStatus) ||
               "ERROR".equals(upperStatus);
    }

    /**
     * Extracts the status value from Jackson parsing error messages.
     * Handles errors like "Unexpected value 'FINISHED'"
     */
    private String extractStatusFromError(String errorMessage) {
        if (errorMessage == null) {
            return "UNKNOWN";
        }

        // Look for pattern: Unexpected value 'STATUS'
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Unexpected value '([^']+)'");
        java.util.regex.Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "UNKNOWN";
    }
}
