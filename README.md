# Cursor Background Agents API for Java

A repository to help Java developers to interact with Cursor Background Agents REST API.

```java
public class LaunchAgentExample {

    private static final String DEFAULT_API_BASE_URL = "https://api.cursor.com";
    private static final String PROMPT_TEXT = """
                                              Create a Java Hello World program
                                              and verify the results compiling and executing
                                              """;
    private static final String REPOSITORY_URL = "https://github.com/jabrena/cursor-background-agent-api-java-hello-world";
    private static final String REPOSITORY_BRANCH = "main";

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

            // Create the launch request
            LaunchAgentRequest request = new LaunchAgentRequest();
            request.setPrompt(prompt);
            request.setSource(source);

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
```

## Cursor API References

- https://docs.cursor.com/en/background-agent/api/list-agents
- https://docs.cursor.com/en/background-agent/api/agent-status
- https://docs.cursor.com/en/background-agent/api/agent-conversation
- https://docs.cursor.com/en/background-agent/api/launch-an-agent
- https://docs.cursor.com/en/background-agent/api/add-followup
- https://docs.cursor.com/en/background-agent/api/delete-agent
- https://docs.cursor.com/en/background-agent/api/api-key-info
- https://docs.cursor.com/en/background-agent/api/list-models
- https://docs.cursor.com/en/background-agent/api/list-repositories

## References

- https://openapi-generator.tech/docs/plugins/
- https://openapi-generator.tech/docs/generators/java
- https://editor-next.swagger.io/
- https://github.com/jabrena/cursor-rules-java
