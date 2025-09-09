package com.example.client;

import com.example.client.api.CursorsApi;
import com.example.client.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WireMock integration test for CursorsApi using payload examples from OpenAPI YAML
 */
public class CursorsApiWireMockTest {

    private WireMockServer wireMockServer;
    private CursorsApi cursorsApi;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8080));
        wireMockServer.start();
        
        // Configure WireMock
        WireMock.configureFor("localhost", 8080);
        
        // Create ObjectMapper for JSON serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // Create API client pointing to WireMock server
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:8080");
        cursorsApi = new CursorsApi(apiClient);
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testListCursors_Success() throws Exception {
        // Prepare mock response using examples from OpenAPI YAML
        CursorListResponse mockResponse = createMockCursorListResponse();
        
        stubFor(get(urlEqualTo("/cursors?page=1&limit=10"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute API call
        CursorListResponse response = cursorsApi.listCursors(1, 10);

        // Verify response
        assertNotNull(response);
        assertEquals(2, response.getCursors().size());
        
        Cursor firstCursor = response.getCursors().get(0);
        assertEquals("cursor-001", firstCursor.getId());
        assertEquals("Default Cursor", firstCursor.getName());
        assertEquals(Cursor.TypeEnum.POINTER, firstCursor.getType());
        assertEquals(100, firstCursor.getPosition().getX());
        assertEquals(200, firstCursor.getPosition().getY());
        assertTrue(firstCursor.getActive());
        
        Cursor secondCursor = response.getCursors().get(1);
        assertEquals("cursor-002", secondCursor.getId());
        assertEquals("Text Cursor", secondCursor.getName());
        assertEquals(Cursor.TypeEnum.TEXT, secondCursor.getType());
        assertEquals(250, secondCursor.getPosition().getX());
        assertEquals(150, secondCursor.getPosition().getY());
        assertFalse(secondCursor.getActive());
        
        Pagination pagination = response.getPagination();
        assertEquals(1, pagination.getPage());
        assertEquals(10, pagination.getLimit());
        assertEquals(2, pagination.getTotal());
        assertEquals(1, pagination.getTotalPages());

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/cursors?page=1&limit=10")));
    }

    @Test
    public void testListCursors_BadRequest() throws Exception {
        // Mock error response using example from OpenAPI YAML
        ErrorResponse errorResponse = createMockErrorResponse(
            "INVALID_PARAMETER", 
            "Page parameter must be a positive integer",
            "The 'page' query parameter must be greater than 0"
        );

        stubFor(get(urlEqualTo("/cursors?page=0&limit=10"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(errorResponse))));

        // Execute and verify exception
        ApiException exception = assertThrows(ApiException.class, () -> {
            cursorsApi.listCursors(0, 10);
        });
        
        assertEquals(400, exception.getCode());
        verify(getRequestedFor(urlEqualTo("/cursors?page=0&limit=10")));
    }

    @Test
    public void testCreateCursor_Success() throws Exception {
        // Prepare request using example from OpenAPI YAML
        CreateCursorRequest request = createMockCreateCursorRequest();
        
        // Prepare response using example from OpenAPI YAML
        Cursor mockResponse = createMockCursor(
            "cursor-003",
            "Gaming Cursor", 
            Cursor.TypeEnum.POINTER,
            300, 400, true,
            "2024-01-15T12:00:00Z",
            "2024-01-15T12:00:00Z"
        );

        stubFor(post(urlEqualTo("/cursors"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute API call
        Cursor response = cursorsApi.createCursor(request);

        // Verify response
        assertNotNull(response);
        assertEquals("cursor-003", response.getId());
        assertEquals("Gaming Cursor", response.getName());
        assertEquals(Cursor.TypeEnum.POINTER, response.getType());
        assertEquals(300, response.getPosition().getX());
        assertEquals(400, response.getPosition().getY());
        assertTrue(response.getActive());

        // Verify the request was made with correct body
        verify(postRequestedFor(urlEqualTo("/cursors"))
            .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    public void testCreateCursor_ValidationError() throws Exception {
        CreateCursorRequest request = createMockCreateCursorRequest();
        
        ErrorResponse errorResponse = createMockErrorResponse(
            "VALIDATION_ERROR",
            "Invalid cursor data provided",
            "Name field is required and cannot be empty"
        );

        stubFor(post(urlEqualTo("/cursors"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(errorResponse))));

        // Execute and verify exception
        ApiException exception = assertThrows(ApiException.class, () -> {
            cursorsApi.createCursor(request);
        });
        
        assertEquals(400, exception.getCode());
        verify(postRequestedFor(urlEqualTo("/cursors")));
    }

    @Test
    public void testGetCursorById_Success() throws Exception {
        String cursorId = "cursor-001";
        
        // Prepare response using example from OpenAPI YAML
        Cursor mockResponse = createMockCursor(
            cursorId,
            "Default Cursor",
            Cursor.TypeEnum.POINTER,
            100, 200, true,
            "2024-01-15T10:30:00Z",
            "2024-01-15T10:30:00Z"
        );

        stubFor(get(urlEqualTo("/cursors/" + cursorId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute API call
        Cursor response = cursorsApi.getCursorById(cursorId);

        // Verify response
        assertNotNull(response);
        assertEquals(cursorId, response.getId());
        assertEquals("Default Cursor", response.getName());

        verify(getRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    public void testGetCursorById_NotFound() throws Exception {
        String cursorId = "cursor-999";
        
        ErrorResponse errorResponse = createMockErrorResponse(
            "CURSOR_NOT_FOUND",
            "Cursor with specified ID not found",
            "No cursor exists with ID 'cursor-999'"
        );

        stubFor(get(urlEqualTo("/cursors/" + cursorId))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(errorResponse))));

        // Execute and verify exception
        ApiException exception = assertThrows(ApiException.class, () -> {
            cursorsApi.getCursorById(cursorId);
        });
        
        assertEquals(404, exception.getCode());
        verify(getRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    public void testUpdateCursor_Success() throws Exception {
        String cursorId = "cursor-001";
        
        // Prepare request using example from OpenAPI YAML
        UpdateCursorRequest request = createMockUpdateCursorRequest();
        
        // Prepare response using example from OpenAPI YAML
        Cursor mockResponse = createMockCursor(
            cursorId,
            "Updated Cursor",
            Cursor.TypeEnum.POINTER,
            500, 600, false,
            "2024-01-15T10:30:00Z",
            "2024-01-15T13:15:00Z"
        );

        stubFor(put(urlEqualTo("/cursors/" + cursorId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute API call
        Cursor response = cursorsApi.updateCursor(cursorId, request);

        // Verify response
        assertNotNull(response);
        assertEquals(cursorId, response.getId());
        assertEquals("Updated Cursor", response.getName());
        assertEquals(500, response.getPosition().getX());
        assertEquals(600, response.getPosition().getY());
        assertFalse(response.getActive());

        verify(putRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    public void testDeleteCursor_Success() throws Exception {
        String cursorId = "cursor-001";

        stubFor(delete(urlEqualTo("/cursors/" + cursorId))
            .willReturn(aResponse()
                .withStatus(204)));

        // Execute API call - should not throw exception
        assertDoesNotThrow(() -> {
            cursorsApi.deleteCursor(cursorId);
        });

        verify(deleteRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    public void testDeleteCursor_NotFound() throws Exception {
        String cursorId = "cursor-999";
        
        ErrorResponse errorResponse = createMockErrorResponse(
            "CURSOR_NOT_FOUND",
            "Cursor with specified ID not found",
            "No cursor exists with ID 'cursor-999'"
        );

        stubFor(delete(urlEqualTo("/cursors/" + cursorId))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(errorResponse))));

        // Execute and verify exception
        ApiException exception = assertThrows(ApiException.class, () -> {
            cursorsApi.deleteCursor(cursorId);
        });
        
        assertEquals(404, exception.getCode());
        verify(deleteRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    public void testMoveCursor_Success() throws Exception {
        String cursorId = "cursor-001";
        
        // Prepare request using example from OpenAPI YAML
        MoveCursorRequest request = createMockMoveCursorRequest();
        
        // Prepare response using example from OpenAPI YAML
        Cursor mockResponse = createMockCursor(
            cursorId,
            "Default Cursor",
            Cursor.TypeEnum.POINTER,
            750, 850, true,
            "2024-01-15T10:30:00Z",
            "2024-01-15T13:30:00Z"
        );

        stubFor(post(urlEqualTo("/cursors/" + cursorId + "/move"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Execute API call
        Cursor response = cursorsApi.moveCursor(cursorId, request);

        // Verify response
        assertNotNull(response);
        assertEquals(cursorId, response.getId());
        assertEquals(750, response.getPosition().getX());
        assertEquals(850, response.getPosition().getY());

        verify(postRequestedFor(urlEqualTo("/cursors/" + cursorId + "/move")));
    }

    // Helper methods to create mock objects using examples from OpenAPI YAML

    private CursorListResponse createMockCursorListResponse() {
        CursorListResponse response = new CursorListResponse();
        
        List<Cursor> cursors = Arrays.asList(
            createMockCursor("cursor-001", "Default Cursor", Cursor.TypeEnum.POINTER, 
                           100, 200, true, "2024-01-15T10:30:00Z", "2024-01-15T10:30:00Z"),
            createMockCursor("cursor-002", "Text Cursor", Cursor.TypeEnum.TEXT, 
                           250, 150, false, "2024-01-15T09:15:00Z", "2024-01-15T11:45:00Z")
        );
        response.setCursors(cursors);
        
        Pagination pagination = new Pagination();
        pagination.setPage(1);
        pagination.setLimit(10);
        pagination.setTotal(2);
        pagination.setTotalPages(1);
        response.setPagination(pagination);
        
        return response;
    }

    private Cursor createMockCursor(String id, String name, Cursor.TypeEnum type, 
                                   int x, int y, boolean active, 
                                   String createdAt, String updatedAt) {
        Cursor cursor = new Cursor();
        cursor.setId(id);
        cursor.setName(name);
        cursor.setType(type);
        
        Position position = new Position();
        position.setX(x);
        position.setY(y);
        cursor.setPosition(position);
        
        cursor.setActive(active);
        cursor.setCreatedAt(OffsetDateTime.parse(createdAt));
        cursor.setUpdatedAt(OffsetDateTime.parse(updatedAt));
        
        return cursor;
    }

    private CreateCursorRequest createMockCreateCursorRequest() {
        CreateCursorRequest request = new CreateCursorRequest();
        request.setName("Gaming Cursor");
        request.setType(CreateCursorRequest.TypeEnum.POINTER);
        
        Position position = new Position();
        position.setX(300);
        position.setY(400);
        request.setPosition(position);
        
        request.setActive(true);
        return request;
    }

    private UpdateCursorRequest createMockUpdateCursorRequest() {
        UpdateCursorRequest request = new UpdateCursorRequest();
        request.setName("Updated Cursor");
        
        Position position = new Position();
        position.setX(500);
        position.setY(600);
        request.setPosition(position);
        
        request.setActive(false);
        return request;
    }

    private MoveCursorRequest createMockMoveCursorRequest() {
        MoveCursorRequest request = new MoveCursorRequest();
        
        Position position = new Position();
        position.setX(750);
        position.setY(850);
        request.setPosition(position);
        
        request.setAnimate(true);
        request.setDuration(500);
        return request;
    }

    private ErrorResponse createMockErrorResponse(String code, String message, String details) {
        ErrorResponse errorResponse = new ErrorResponse();
        ErrorResponseError error = new ErrorResponseError();
        error.setCode(code);
        error.setMessage(message);
        error.setDetails(details);
        errorResponse.setError(error);
        return errorResponse;
    }
}