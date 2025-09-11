package info.jab.cursor.client;

import info.jab.cursor.client.api.AgentsApi;
import info.jab.cursor.client.model.Agent;
import info.jab.cursor.client.model.LaunchAgentRequest;
import info.jab.cursor.client.model.Prompt;
import info.jab.cursor.client.model.Source;
import info.jab.cursor.client.model.TargetRequest;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating how to use the AgentsApi to launch a Cursor agent
 * that creates a Java Hello World program and verifies compilation/execution.
 *
 * This example shows:
 * - How to configure the API client
 * - How to create a LaunchAgentRequest with prompt, source, model, target
 * - How to handle the API response
 */
public class LaunchAgentExample {

    private static final String DEFAULT_API_BASE_URL = "https://api.cursor.com";
    private static final String PROMPT_TEXT = """
                                              Create a Java Hello World program
                                              and verify the results compiling and executing
                                              """;
    private static final String REPOSITORY_URL = "https://github.com/jabrena/cursor-background-agent-api-java-hello-world";
    private static final String REPOSITORY_BRANCH = "main";
    private static final String DEFAULT_MODEL = "claude-4-sonnet";

    public static void main(String[] args) {
        try {
            // Get API key from command line argument or use example key
            String apiKey = args.length > 0 ? args[0] : "EXAMPLE_API_KEY";

            // Create and configure API client
            ApiClient apiClient = new ApiClient();
            apiClient.updateBaseUri(DEFAULT_API_BASE_URL);
            AgentsApi agentsApi = new AgentsApi(apiClient);

            // Create the prompt
            Prompt prompt = new Prompt();
            prompt.setText(PROMPT_TEXT);

            // Create the source (repository and branch)
            Source source = new Source();
            source.setRepository(URI.create(REPOSITORY_URL));
            source.setRef(REPOSITORY_BRANCH);

            // Create the target configuration (optional)
            TargetRequest target = new TargetRequest();
            target.setAutoCreatePr(true);  // Automatically create PR when agent completes

            // Create the launch request
            LaunchAgentRequest request = new LaunchAgentRequest();
            request.setPrompt(prompt);
            request.setSource(source);
            request.setModel(DEFAULT_MODEL);  // Specify the LLM model to use (optional)
            request.setTarget(target);  // Set target configuration (optional)

            // Launch the agent (with authentication headers)
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            Agent agent = agentsApi.launchAgent(request, headers);

            // Display the response
            System.out.println("Agent launched: " + agent.getId() + " (" + agent.getStatus() + ")");
            System.out.println("Monitor at: " + agent.getTarget().getUrl());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error launching agent: " + e.getMessage());
            System.exit(1);
        }
    }
}
