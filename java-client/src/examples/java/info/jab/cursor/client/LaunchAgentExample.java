package info.jab.cursor.client;

import info.jab.cursor.CursorAgentManagementClient;

public class LaunchAgentExample {

    public static void main(String[] args) {
        // Get API key from command line argument or use example key
        String apiKey = args.length > 0 ? args[0] : "EXAMPLE_API_KEY";

        var client = new CursorAgentManagementClient(apiKey);

        var userPrompt = """
                            Create a Java Hello World program
                            and verify the results compiling and executing
                            """;
        var repository = "https://github.com/jabrena/cursor-background-agent-api-java-hello-world";
        var defaultModel = "claude-4-sonnet";

        var agentResult = client.launch(userPrompt, defaultModel, repository);

        // Use fold to handle both success and failure cases elegantly
        String message = agentResult.fold(
            agent -> "✅ Agent created successfully: " + agent.id() + " (" + agent.status() + ")",
            error -> "❌ Failed to create agent: " + error.getMessage()
        );
        System.out.println(message);

        // Exit with appropriate code based on result
        int exitCode = agentResult.fold(
            agent -> 0,  // Success
            error -> 1   // Failure
        );
        System.exit(exitCode);
    }
}
