package info.jab.cursor.client;

import info.jab.cursor.CursorAgentManagementClient;
import info.jab.cursor.client.model.CreateAgent201Response;

/**
 * Simplified example demonstrating how to use the CursorAgentManagementClient to launch a Cursor agent
 * that creates a Java Hello World program and verifies compilation/execution.
 *
 * This example shows:
 * - How to use the high-level CursorAgentManagementClient
 * - How to launch an agent with just 3 parameters: prompt, model, and repository
 * - How to handle the response
 */
public class LaunchAgentExample {

    private static final String PROMPT_TEXT = """
                                              Create a Java Hello World program
                                              and verify the results compiling and executing
                                              """;
    private static final String REPOSITORY_URL = "https://github.com/jabrena/cursor-background-agent-api-java-hello-world";
    private static final String DEFAULT_MODEL = "claude-4-sonnet";

    public static void main(String[] args) {
        try {
            // Get API key from command line argument or use example key
            String apiKey = args.length > 0 ? args[0] : "EXAMPLE_API_KEY";

            // Create the high-level client - much simpler!
            var client = new CursorAgentManagementClient(apiKey);

            // Launch the agent with just 3 parameters
            var agent = client.launch(PROMPT_TEXT, DEFAULT_MODEL, REPOSITORY_URL);

            // Display the response
            System.out.println("Agent created: " + agent.getId() + " (" + agent.getStatus() + ")");
            System.out.println("Monitor at: " + agent.getTarget().getUrl());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error creating agent: " + e.getMessage());
            System.exit(1);
        }
    }
}
