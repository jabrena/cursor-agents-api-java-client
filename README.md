# Cursor Background Agents API - OpenAPI Specification

This repository contains the OpenAPI 3.0.3 specification for the Cursor Background Agents API, created by analyzing the official Cursor documentation.

## 📋 Overview

The Cursor Background Agents API allows you to programmatically manage AI-powered background agents that can work autonomously on your repositories. These agents can perform tasks like:

- Code generation and refactoring
- Documentation updates
- Bug fixes and improvements  
- Feature implementation
- Code reviews and analysis

## 📁 Files

- **`cursor-api-openapi.yaml`** - OpenAPI specification in YAML format
- **`cursor-api-openapi.json`** - OpenAPI specification in JSON format
- **`README.md`** - This documentation file

## 🚀 API Endpoints

The specification includes the following endpoints:

### Agent Management
- **`GET /v0/agents`** - List all background agents
- **`POST /v0/agents`** - Launch a new background agent
- **`GET /v0/agents/{id}`** - Get agent status
- **`DELETE /v0/agents/{id}`** - Delete an agent
- **`GET /v0/agents/{id}/conversation`** - Get agent conversation history
- **`POST /v0/agents/{id}/followup`** - Add follow-up instructions to an agent

### Authentication & Metadata  
- **`GET /v0/api-key`** - Get API key information
- **`GET /v0/models`** - List available AI models
- **`GET /v0/repositories`** - List accessible repositories

## 🔧 Using the OpenAPI Specification

### 1. API Documentation
You can use tools like [Swagger UI](https://swagger.io/tools/swagger-ui/) or [Redoc](https://redocly.com/redoc/) to generate interactive API documentation:

```bash
# Using Docker with Swagger UI
docker run -p 8080:8080 -e SWAGGER_JSON=/api/cursor-api-openapi.yaml -v $(pwd):/api swaggerapi/swagger-ui

# Access at http://localhost:8080
```

### 2. Client Code Generation
Generate client libraries in various programming languages using [OpenAPI Generator](https://openapi-generator.tech/):

```bash
# Install OpenAPI Generator
npm install @openapitools/openapi-generator-cli -g

# Generate Python client
openapi-generator-cli generate -i cursor-api-openapi.yaml -g python -o ./python-client

# Generate JavaScript client
openapi-generator-cli generate -i cursor-api-openapi.yaml -g javascript -o ./js-client

# Generate Go client
openapi-generator-cli generate -i cursor-api-openapi.yaml -g go -o ./go-client
```

### 3. API Testing
Use tools like [Postman](https://www.postman.com/) or [Insomnia](https://insomnia.rest/) to import the OpenAPI spec and test the API:

1. Import the `cursor-api-openapi.yaml` or `cursor-api-openapi.json` file
2. Configure authentication with your Cursor API token
3. Test the endpoints directly from the interface

### 4. Mock Server
Create a mock server for testing and development:

```bash
# Using Prism
npm install -g @stoplight/prism-cli
prism mock cursor-api-openapi.yaml

# Mock server will be available at http://localhost:4010
```

## 🔐 Authentication

All API endpoints require authentication using a Bearer token:

```bash
curl -H "Authorization: Bearer YOUR_API_TOKEN" \
     https://api.cursor.com/v0/agents
```

Get your API token from the [Cursor dashboard](https://cursor.com/settings/api).

## 📊 API Features

### Pagination
List endpoints use cursor-based pagination:
```json
{
  "agents": [...],
  "nextCursor": "bc_def456"
}
```

### Error Handling
The API returns structured error responses:
```json
{
  "error": {
    "code": "invalid_request",
    "message": "The request is missing required parameters",
    "details": {...}
  }
}
```

### Rate Limiting
The API implements rate limiting with appropriate headers:
- `Retry-After` header indicates when to retry after hitting limits

## 🧪 Example Usage

### Launch a New Agent
```bash
curl -X POST https://api.cursor.com/v0/agents \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": {
      "text": "Add comprehensive unit tests for the authentication module"
    },
    "source": {
      "repository": "https://github.com/your-org/your-repo",
      "ref": "main"
    }
  }'
```

### Check Agent Status
```bash
curl https://api.cursor.com/v0/agents/bc_abc123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Add Follow-up Instructions
```bash
curl -X POST https://api.cursor.com/v0/agents/bc_abc123/followup \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": {
      "text": "Also add integration tests for the API endpoints"
    }
  }'
```

## 📚 Schema Definitions

The specification includes comprehensive schema definitions for:

- **Agent** - Background agent with status, source, target, and metadata
- **Prompt** - Instructions with optional image attachments
- **Message** - Conversation messages between user and agent
- **Repository** - Repository information and metadata
- **Model** - Available AI models and capabilities
- **ApiKeyInfo** - API key permissions and metadata

## ✅ Validation

The OpenAPI specification has been validated for:
- ✅ YAML syntax correctness
- ✅ OpenAPI 3.0.3 compliance
- ✅ Required field presence
- ✅ Schema consistency
- ✅ Endpoint completeness (9 endpoints across 7 paths)
- ✅ Comprehensive error handling

## 📖 Documentation Sources

This OpenAPI specification was created by analyzing the official Cursor documentation:

- [List Agents](https://docs.cursor.com/en/background-agent/api/list-agents)
- [Agent Status](https://docs.cursor.com/en/background-agent/api/agent-status)
- [Agent Conversation](https://docs.cursor.com/en/background-agent/api/agent-conversation)
- [Launch an Agent](https://docs.cursor.com/en/background-agent/api/launch-an-agent)
- [Add Follow-up](https://docs.cursor.com/en/background-agent/api/add-followup)
- [Delete Agent](https://docs.cursor.com/en/background-agent/api/delete-agent)
- [API Key Info](https://docs.cursor.com/en/background-agent/api/api-key-info)
- [List Models](https://docs.cursor.com/en/background-agent/api/list-models)
- [List Repositories](https://docs.cursor.com/en/background-agent/api/list-repositories)

## 🤝 Contributing

To update this specification:

1. Modify the `cursor-api-openapi.yaml` file
2. Regenerate the JSON version: `python3 -c "import yaml, json; json.dump(yaml.safe_load(open('cursor-api-openapi.yaml')), open('cursor-api-openapi.json', 'w'), indent=2)"`
3. Validate the changes: `python3 -c "import yaml; yaml.safe_load(open('cursor-api-openapi.yaml'))"`

## 📄 License

This OpenAPI specification is provided under the MIT License. See the specification file for details.

---

*Generated from Cursor Background Agents API documentation - Last updated: $(date)*