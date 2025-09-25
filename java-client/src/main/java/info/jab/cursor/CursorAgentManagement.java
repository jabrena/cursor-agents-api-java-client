package info.jab.cursor;

import info.jab.cursor.client.model.CreateAgent201Response;
import info.jab.cursor.client.model.DeleteAgent200Response;

import info.jab.control.Result;

/**
 * Interface for agent management operations.
 *
 * @see <a href="https://cursor.com/en/docs/background-agent/api/endpoints">Background Agents API Endpoints</a>
 */
public interface CursorAgentManagement {

    /**
     * Launches a Cursor agent with the specified parameters.
     *
     * @param prompt The prompt/instructions for the agent to execute
     * @param model The LLM model to use (e.g., "claude-4-sonnet")
     * @param repository The repository URL where the agent should work
     * @return Result containing LaunchResponse representing the launched agent
     */
    Result<LaunchResponse> launch(String prompt, String model, String repository);

    /**
     * Adds a follow-up prompt to an existing agent.
     *
     * @param agentId The ID of the agent to add follow-up to
     * @param prompt The follow-up prompt/instructions
     * @return Result containing DeleteAgent200Response with the result of the follow-up
     */
    Result<String> followUp(String agentId, String prompt);

    /**
     * Deletes a Cursor agent by its ID.
     *
     * @param agentId The ID of the agent to delete
     * @return Result containing DeleteAgent200Response with the result of the deletion
     */
    Result<String> delete(String agentId);
}
