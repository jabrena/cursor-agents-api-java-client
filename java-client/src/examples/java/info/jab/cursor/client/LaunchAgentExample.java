package info.jab.cursor.client;

import info.jab.cursor.CursorAgentManagementClient;

public class LaunchAgentExample {

    public static void main(String[] args) {
        try {
            // Get API key from command line argument or use example key
            String apiKey = args.length > 0 ? args[0] : "EXAMPLE_API_KEY";

            var client = new CursorAgentManagementClient(apiKey);

            var userPrompt = """
                             Create a Java Hello World program
                             and verify the results compiling and executing
                             """;
            var repository = "https://github.com/jabrena/cursor-background-agent-api-java-hello-world";
            var defaultModel = "claude-4-sonnet";

            var agent = client.launch(userPrompt, repository, repository);

            System.out.println("Agent created: " + agent.id() + " (" + agent.status() + ")");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error creating agent: " + e.getMessage());
            System.exit(1);
        }
    }
}
