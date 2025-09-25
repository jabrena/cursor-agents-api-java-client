package info.jab.cursor;

import info.jab.cursor.client.ApiClient;
import info.jab.cursor.client.api.DefaultApi;
import info.jab.cursor.client.model.CreateAgent201Response;
import info.jab.cursor.client.model.CreateAgentRequest;
import info.jab.cursor.client.model.CreateAgentRequestPrompt;
import info.jab.cursor.client.model.CreateAgentRequestSource;
import info.jab.cursor.client.model.CreateAgentRequestTarget;
import info.jab.cursor.client.model.AddFollowupRequest;
import info.jab.cursor.client.model.AddFollowupRequestPrompt;
import info.jab.cursor.client.model.DeleteAgent200Response;

import java.util.HashMap;
import java.util.Map;

import info.jab.control.Result;

/**
 * Client implementation for Cursor Agent Management operations.
 * This class provides access to agent management APIs including launching agents,
 * adding follow-ups, and deleting agents.
 */
public class CursorAgentManagementClient implements CursorAgentManagement {

    private static final String DEFAULT_API_BASE_URL = "https://api.cursor.com";
    private static final String DEFAULT_BRANCH = "main";

    private final String apiKey;
    private final String apiBaseUrl;
    private final DefaultApi defaultApi;

    /**
     * Creates a new CursorAgentManagementClient with the specified API key.
     * Uses the default API base URL.
     *
     * @param apiKey The API key for authentication with Cursor API
     */
    public CursorAgentManagementClient(String apiKey) {
        this(apiKey, DEFAULT_API_BASE_URL);
    }

    /**
     * Creates a new CursorAgentManagementClient with the specified API key and base URL.
     *
     * @param apiKey The API key for authentication with Cursor API
     * @param apiBaseUrl The base URL for the Cursor API
     */
    public CursorAgentManagementClient(String apiKey, String apiBaseUrl) {
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;

        // Initialize API client
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(apiBaseUrl);

        this.defaultApi = new DefaultApi(apiClient);
    }

    /**
     * Launches a Cursor agent with the specified parameters.
     *
     * @param prompt The prompt/instructions for the agent to execute
     * @param model The LLM model to use (e.g., "claude-4-sonnet")
     * @param repository The repository URL where the agent should work
     * @return Result containing LaunchResponse representing the launched agent
     */
    @Override
    public Result<LaunchResponse> launch(String prompt, String model, String repository) {
        // Validate inputs
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (repository == null || repository.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository cannot be null or empty");
        }

        // Create the prompt
        CreateAgentRequestPrompt promptObj = new CreateAgentRequestPrompt();
        promptObj.setText(prompt);

        // Create the source (repository and branch)
        CreateAgentRequestSource source = new CreateAgentRequestSource();
        source.setRepository(repository);
        source.setRef(DEFAULT_BRANCH);

        // Create the target configuration (optional)
        CreateAgentRequestTarget target = new CreateAgentRequestTarget();
        target.setAutoCreatePr(true);  // Automatically create PR when agent completes

        // Create the launch request
        CreateAgentRequest request = new CreateAgentRequest();
        request.setPrompt(promptObj);
        request.setSource(source);
        request.setModel(model);
        request.setTarget(target);

        // Prepare authentication headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        // Launch the agent
        return Result.runCatching(() -> defaultApi.createAgent(request, headers))
                .map(response -> new LaunchResponse(response.getId(), response.getStatus().getValue()));
    }

    /**
     * Adds a follow-up prompt to an existing agent.
     *
     * @param agentId The ID of the agent to add follow-up to
     * @param prompt The follow-up prompt/instructions
     * @return Result containing DeleteAgent200Response with the result of the follow-up
     */
    @Override
    public Result<String> followUp(String agentId, String prompt) {
        // Validate inputs
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent ID cannot be null or empty");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }

        // Create the prompt
        AddFollowupRequestPrompt promptObj = new AddFollowupRequestPrompt();
        promptObj.setText(prompt);

        // Create the follow-up request
        AddFollowupRequest request = new AddFollowupRequest();
        request.setPrompt(promptObj);

        // Prepare authentication headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        // Follow-up the agent
        return Result.runCatching(() -> defaultApi.addFollowup(agentId, request, headers))
                    .map(response -> response.getId());
    }

    /**
     * Deletes a Cursor agent by its ID.
     *
     * @param agentId The ID of the agent to delete
     * @return Result containing DeleteAgent200Response with the result of the deletion
     */
    @Override
    public Result<String> delete(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent ID cannot be null or empty");
        }

        // Prepare authentication headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);

        return Result.runCatching(() -> defaultApi.deleteAgent(agentId, headers))
                    .map(response -> response.getId());
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
}
