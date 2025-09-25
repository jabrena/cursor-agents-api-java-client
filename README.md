# Cursor Background Agents API for Java

A repository to help Java developers to interact with Cursor Background Agents REST API.

Cursor provides the following REST endpoints:

- https://cursor.com/en/docs/background-agent/api/endpoints

and provides an OpenAPI Specification: https://cursor.com/docs-static/background-agents-openapi.yaml

This project offer the following value:

- [Swagger UI](https://jabrena.github.io/cursor-agents-api-java-client/) to understand better how to interact the different endpoints.
- Java Client to interact with Cursor Background Agent REST API

## Quick Start

### 1. Get Your API Key

- Go to [Cursor Settings](https://cursor.com/settings) → API section
- Copy your API key

### 2. Get familiar with the REST API.

- https://jabrena.github.io/cursor-agents-api-java-client/

### 3. Test an example in your terminal

```bash
curl -X 'POST' \
  'https://api.cursor.com/v0/agents' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{
  "prompt": {
    "text": "Create a Java Hello World program and verify the results compiling and executing"
  },
  "source": {
    "repository": "https://github.com/jabrena/cursor-background-agent-api-java-hello-world",
    "ref": "main"
  },
  "model": "claude-4-sonnet",
  "target": {
    "autoCreatePr": true
  }
}'
```

### 3. Test an example with Java

Review the example from this repository:

```java
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
```

And execute in the terminal:

```bash
./mvnw clean compile exec:java -Pexamples -pl java-client -Dexec.args="YOUR_CURSOR_KEY"
```

You should see somethink like this:

```bash
Agent launched: bc-fd85dce9-64d0-4c73-86ca-f74d87fa1ddb (CREATING)
Monitor at: https://cursor.com/agents?id=bc-fd85dce9-64d0-4c73-86ca-f74d87fa1ddb
```

## References

- https://openapi-generator.tech/docs/plugins/
- https://openapi-generator.tech/docs/generators/java
- https://editor-next.swagger.io/
- https://github.com/jabrena/cursor-rules-java
