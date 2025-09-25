package info.jab.cursor;

import info.jab.cursor.client.model.ListAgents200ResponseAgentsInner;
import info.jab.cursor.client.model.ListAgents200Response;
import info.jab.cursor.client.model.GetAgentConversation200Response;

/**
 * Interface for agent information operations.
 *
 * @see <a href="https://cursor.com/en/docs/background-agent/api/endpoints">Background Agents API Endpoints</a>
 */
public interface CursorAgentInformation {

    /**
     * Gets a list of agents with optional pagination.
     *
     * @param limit Maximum number of agents to return (optional, can be null)
     * @param cursor Pagination cursor for retrieving next page (optional, can be null)
     * @return ListAgents200Response containing the list of agents
     * @throws Exception if the operation fails
     */
    ListAgents200Response getAgents(Integer limit, String cursor) throws Exception;

    /**
     * Gets the current status of a specific agent.
     *
     * @param agentId The ID of the agent to retrieve status for
     * @return ListAgents200ResponseAgentsInner instance with current status information
     * @throws Exception if the operation fails
     */
    ListAgents200ResponseAgentsInner getStatus(String agentId) throws Exception;

    /**
     * Gets the conversation history for a specific agent.
     *
     * @param agentId The ID of the agent to retrieve conversation for
     * @return GetAgentConversation200Response containing the agent's conversation history
     * @throws Exception if the operation fails
     */
    GetAgentConversation200Response getAgentConversation(String agentId) throws Exception;

}
