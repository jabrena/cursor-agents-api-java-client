package info.jab.cursor.client;

import info.jab.cursor.client.api.AgentsApi;
import info.jab.cursor.client.model.Agent;
import info.jab.cursor.client.model.LaunchAgentRequest;
import info.jab.cursor.client.model.Prompt;
import info.jab.cursor.client.model.Source;

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
 * - How to create a LaunchAgentRequest with proper prompt and source
 * - How to handle the API response
 *
 * Usage:
 * java -cp target/classes:target/dependency/* info.jab.cursor.client.LaunchAgentExample [API_KEY]
 */
public class LaunchAgentExample {

    // Default API base URL - can be overridden via environment variable
    private static final String DEFAULT_API_BASE_URL = "https://api.cursor.com";

    // Example API key for demonstration - replace with your actual API key
    private static final String EXAMPLE_API_KEY = "cur_abc123def456ghi789jkl012mno345pqr678stu901vwx234yz";

    public static void main(String[] args) {
        try {
            // Get API key from command line argument or use example key
            String apiKey = args.length > 0 ? args[0] : EXAMPLE_API_KEY;

            System.out.println("=== Cursor Agent Launch Example ===");
            System.out.println("API Key: " + maskApiKey(apiKey));

            // Create and configure API client
            ApiClient apiClient = createApiClient(apiKey);
            AgentsApi agentsApi = new AgentsApi(apiClient);

            // Create the launch request
            LaunchAgentRequest request = createLaunchAgentRequest();

            System.out.println("\nLaunching agent with request:");
            System.out.println("Repository: " + request.getSource().getRepository());
            System.out.println("Branch: " + request.getSource().getRef());
            System.out.println("Prompt: " + request.getPrompt().getText());

            // Launch the agent (with authentication headers)
            System.out.println("\nSending request to Cursor API...");
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            Agent agent = agentsApi.launchAgent(request, headers);

            // Display the response
            displayAgentInfo(agent);

            System.out.println("\n=== Agent launched successfully! ===");
            System.out.println("You can monitor the agent's progress at: " + agent.getTarget().getUrl());

        } catch (Exception e) {
            System.err.println("Error launching agent: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates and configures the API client with authentication
     */
    private static ApiClient createApiClient(String apiKey) {
        ApiClient apiClient = new ApiClient();

        // Set base URL (can be overridden via environment variable)
        String baseUrl = System.getenv("CURSOR_API_BASE_URL");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = DEFAULT_API_BASE_URL;
        }
        apiClient.updateBaseUri(baseUrl);

        // Note: Authentication is handled via headers in the API call

        // Configure timeouts (optional)
        apiClient.setConnectTimeout(Duration.ofSeconds(30)); // 30 seconds
        apiClient.setReadTimeout(Duration.ofSeconds(60));    // 60 seconds

        System.out.println("API Base URL: " + baseUrl);

        return apiClient;
    }

    /**
     * Creates a LaunchAgentRequest for the Java Hello World task
     */
    private static LaunchAgentRequest createLaunchAgentRequest() {
        // Create the prompt
        Prompt prompt = new Prompt();
        prompt.setText("Create a Java Hello World program and verify the results compiling and executing");

        // Create the source (repository and branch)
        Source source = new Source();
        source.setRepository(URI.create("https://github.com/jabrena/cursor-background-agent-api-java-hello-world"));
        source.setRef("main");

        // Create the launch request
        LaunchAgentRequest request = new LaunchAgentRequest();
        request.setPrompt(prompt);
        request.setSource(source);

        return request;
    }

    /**
     * Displays information about the launched agent
     */
    private static void displayAgentInfo(Agent agent) {
        System.out.println("\n=== Agent Information ===");
        System.out.println("ID: " + agent.getId());
        System.out.println("Name: " + agent.getName());
        System.out.println("Status: " + agent.getStatus());
        System.out.println("Created: " + agent.getCreatedAt());

        if (agent.getSource() != null) {
            System.out.println("\nSource:");
            System.out.println("  Repository: " + agent.getSource().getRepository());
            System.out.println("  Branch: " + agent.getSource().getRef());
        }

        if (agent.getTarget() != null) {
            System.out.println("\nTarget:");
            System.out.println("  Branch: " + agent.getTarget().getBranchName());
            System.out.println("  URL: " + agent.getTarget().getUrl());
            System.out.println("  Auto Create PR: " + agent.getTarget().getAutoCreatePr());
        }

        // Note: Summary field is not available in this version of the API
        // if (agent.getSummary() != null && !agent.getSummary().trim().isEmpty()) {
        //     System.out.println("\nSummary: " + agent.getSummary());
        // }
    }

    /**
     * Masks the API key for secure logging
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
