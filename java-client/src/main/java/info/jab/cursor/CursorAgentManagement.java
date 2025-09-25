package info.jab.cursor;

import info.jab.cursor.client.model.CreateAgent201Response;
import info.jab.cursor.client.model.DeleteAgent200Response;

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
     * @return CreateAgent201Response instance representing the launched agent
     * @throws Exception if the agent launch fails
     */
    CreateAgent201Response launch(String prompt, String model, String repository) throws Exception;

    /**
     * Adds a follow-up prompt to an existing agent.
     *
     * @param agentId The ID of the agent to add follow-up to
     * @param prompt The follow-up prompt/instructions
     * @return DeleteAgent200Response containing the result of the follow-up
     * @throws Exception if the follow-up fails
     */
    DeleteAgent200Response followUp(String agentId, String prompt) throws Exception;

    /**
     * Deletes a Cursor agent by its ID.
     *
     * @param agentId The ID of the agent to delete
     * @return DeleteAgent200Response containing the result of the deletion
     * @throws Exception if deletion fails
     */
    DeleteAgent200Response delete(String agentId) throws Exception;
}
