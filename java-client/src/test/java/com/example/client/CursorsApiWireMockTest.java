package com.example.client;

import info.jab.cursor.client.ApiClient;
import info.jab.cursor.client.ApiException;
import info.jab.cursor.client.api.CursorsApi;
import info.jab.cursor.client.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * WireMock integration test for CursorsApi using payload examples from OpenAPI YAML
 */
class CursorsApiWireMockTest {

    private WireMockServer wireMockServer;
    private CursorsApi cursorsApi;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
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
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should return cursor list with pagination when valid parameters provided")
    void should_returnCursorListWithPagination_when_validParametersProvided() throws Exception {
        // Given
        CursorListResponse mockResponse = createMockCursorListResponse();
        stubFor(get(urlEqualTo("/cursors?page=1&limit=10"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(mockResponse))));

        // When
        CursorListResponse response = cursorsApi.listCursors(1, 10);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCursors()).hasSize(2);

        Cursor firstCursor = response.getCursors().get(0);
        assertThat(firstCursor)
            .extracting(Cursor::getId, Cursor::getName, Cursor::getType, Cursor::getActive)
            .containsExactly("cursor-001", "Default Cursor", Cursor.TypeEnum.POINTER, true);
        assertThat(firstCursor.getPosition())
            .extracting(Position::getX, Position::getY)
            .containsExactly(100, 200);

        Cursor secondCursor = response.getCursors().get(1);
        assertThat(secondCursor)
            .extracting(Cursor::getId, Cursor::getName, Cursor::getType, Cursor::getActive)
            .containsExactly("cursor-002", "Text Cursor", Cursor.TypeEnum.TEXT, false);
        assertThat(secondCursor.getPosition())
            .extracting(Position::getX, Position::getY)
            .containsExactly(250, 150);

        Pagination pagination = response.getPagination();
        assertThat(pagination)
            .extracting(Pagination::getPage, Pagination::getLimit, Pagination::getTotal, Pagination::getTotalPages)
            .containsExactly(1, 10, 2, 1);

        verify(getRequestedFor(urlEqualTo("/cursors?page=1&limit=10")));
    }

    @Test
    @DisplayName("Should throw ApiException when invalid page parameter provided")
    void should_throwApiException_when_invalidPageParameterProvided() throws Exception {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> cursorsApi.listCursors(0, 10))
            .isInstanceOf(ApiException.class)
            .extracting(ex -> ((ApiException) ex).getCode())
            .isEqualTo(400);

        verify(getRequestedFor(urlEqualTo("/cursors?page=0&limit=10")));
    }

    @Test
    @DisplayName("Should create cursor successfully when valid request provided")
    void should_createCursorSuccessfully_when_validRequestProvided() throws Exception {
        // Given
        CreateCursorRequest request = createMockCreateCursorRequest();
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

        // When
        Cursor response = cursorsApi.createCursor(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response)
            .extracting(Cursor::getId, Cursor::getName, Cursor::getType, Cursor::getActive)
            .containsExactly("cursor-003", "Gaming Cursor", Cursor.TypeEnum.POINTER, true);
        assertThat(response.getPosition())
            .extracting(Position::getX, Position::getY)
            .containsExactly(300, 400);

        verify(postRequestedFor(urlEqualTo("/cursors"))
            .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("Should throw ApiException when validation error occurs")
    void should_throwApiException_when_validationErrorOccurs() throws Exception {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> cursorsApi.createCursor(request))
            .isInstanceOf(ApiException.class)
            .extracting(ex -> ((ApiException) ex).getCode())
            .isEqualTo(400);

        verify(postRequestedFor(urlEqualTo("/cursors")));
    }

    @Test
    @DisplayName("Should return cursor when valid ID provided")
    void should_returnCursor_when_validIdProvided() throws Exception {
        // Given
        String cursorId = "cursor-001";
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

        // When
        Cursor response = cursorsApi.getCursorById(cursorId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response)
            .extracting(Cursor::getId, Cursor::getName)
            .containsExactly(cursorId, "Default Cursor");

        verify(getRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    @DisplayName("Should throw ApiException when cursor not found")
    void should_throwApiException_when_cursorNotFound() throws Exception {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> cursorsApi.getCursorById(cursorId))
            .isInstanceOf(ApiException.class)
            .extracting(ex -> ((ApiException) ex).getCode())
            .isEqualTo(404);

        verify(getRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    @DisplayName("Should update cursor successfully when valid request provided")
    void should_updateCursorSuccessfully_when_validRequestProvided() throws Exception {
        // Given
        String cursorId = "cursor-001";
        UpdateCursorRequest request = createMockUpdateCursorRequest();
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

        // When
        Cursor response = cursorsApi.updateCursor(cursorId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response)
            .extracting(Cursor::getId, Cursor::getName, Cursor::getActive)
            .containsExactly(cursorId, "Updated Cursor", false);
        assertThat(response.getPosition())
            .extracting(Position::getX, Position::getY)
            .containsExactly(500, 600);

        verify(putRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    @DisplayName("Should delete cursor successfully when valid ID provided")
    void should_deleteCursorSuccessfully_when_validIdProvided() throws Exception {
        // Given
        String cursorId = "cursor-001";
        stubFor(delete(urlEqualTo("/cursors/" + cursorId))
            .willReturn(aResponse()
                .withStatus(204)));

        // When & Then
        assertThatCode(() -> cursorsApi.deleteCursor(cursorId))
            .doesNotThrowAnyException();

        verify(deleteRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    @DisplayName("Should throw ApiException when deleting non-existent cursor")
    void should_throwApiException_when_deletingNonExistentCursor() throws Exception {
        // Given
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

        // When & Then
        assertThatThrownBy(() -> cursorsApi.deleteCursor(cursorId))
            .isInstanceOf(ApiException.class)
            .extracting(ex -> ((ApiException) ex).getCode())
            .isEqualTo(404);

        verify(deleteRequestedFor(urlEqualTo("/cursors/" + cursorId)));
    }

    @Test
    @DisplayName("Should move cursor successfully when valid request provided")
    void should_moveCursorSuccessfully_when_validRequestProvided() throws Exception {
        // Given
        String cursorId = "cursor-001";
        MoveCursorRequest request = createMockMoveCursorRequest();
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

        // When
        Cursor response = cursorsApi.moveCursor(cursorId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(cursorId);
        assertThat(response.getPosition())
            .extracting(Position::getX, Position::getY)
            .containsExactly(750, 850);

        verify(postRequestedFor(urlEqualTo("/cursors/" + cursorId + "/move")));
    }

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @ParameterizedTest(name = "Should handle boundary page values: {0}")
        @ValueSource(ints = {1, Integer.MAX_VALUE})
        @DisplayName("Should handle boundary page values correctly")
        void should_handleBoundaryPageValues_when_validRangeProvided(int page) throws Exception {
            // Given
            CursorListResponse mockResponse = createMockCursorListResponse();
            stubFor(get(urlMatching("/cursors\\?page=" + page + "&limit=10"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(mockResponse))));

            // When
            CursorListResponse response = cursorsApi.listCursors(page, 10);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCursors()).hasSize(2);
        }

        @ParameterizedTest(name = "Should handle boundary limit values: {0}")
        @ValueSource(ints = {1, 100})
        @DisplayName("Should handle boundary limit values correctly")
        void should_handleBoundaryLimitValues_when_validRangeProvided(int limit) throws Exception {
            // Given
            CursorListResponse mockResponse = createMockCursorListResponse();
            stubFor(get(urlMatching("/cursors\\?page=1&limit=" + limit))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(mockResponse))));

            // When
            CursorListResponse response = cursorsApi.listCursors(1, limit);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCursors()).hasSize(2);
        }

        @ParameterizedTest(name = "Should reject invalid page values: {0}")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Should reject invalid page values")
        void should_rejectInvalidPageValues_when_outOfRangeProvided(int invalidPage) throws Exception {
            // Given
            ErrorResponse errorResponse = createMockErrorResponse(
                "INVALID_PARAMETER",
                "Page parameter must be a positive integer",
                "The 'page' query parameter must be greater than 0"
            );
            stubFor(get(urlMatching("/cursors\\?page=" + invalidPage + "&limit=10"))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> cursorsApi.listCursors(invalidPage, 10))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(400);
        }

        @ParameterizedTest(name = "Should handle position boundary values: x={0}, y={1}")
        @CsvSource({
            "0, 0",
            "0, 1080",
            "1920, 0",
            "1920, 1080",
            "-100, -100"
        })
        @DisplayName("Should handle position boundary values correctly")
        void should_handlePositionBoundaryValues_when_validCoordinatesProvided(int x, int y) throws Exception {
            // Given
            String cursorId = "cursor-001";
            MoveCursorRequest request = new MoveCursorRequest();
            Position position = new Position();
            position.setX(x);
            position.setY(y);
            request.setPosition(position);
            request.setAnimate(true);
            request.setDuration(500);

            Cursor mockResponse = createMockCursor(
                cursorId, "Test Cursor", Cursor.TypeEnum.POINTER,
                x, y, true, "2024-01-15T10:30:00Z", "2024-01-15T13:30:00Z"
            );

            stubFor(post(urlEqualTo("/cursors/" + cursorId + "/move"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(mockResponse))));

            // When
            Cursor response = cursorsApi.moveCursor(cursorId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPosition())
                .extracting(Position::getX, Position::getY)
                .containsExactly(x, y);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle network timeout gracefully")
        void should_handleNetworkTimeout_when_serverNotResponding() {
            // Given
            stubFor(get(urlEqualTo("/cursors?page=1&limit=10"))
                .willReturn(aResponse()
                    .withFixedDelay(30000))); // 30 second delay to simulate timeout

            // When & Then
            assertThatThrownBy(() -> cursorsApi.listCursors(1, 10))
                .isInstanceOf(Exception.class); // Could be ApiException or timeout exception
        }

        @ParameterizedTest(name = "Should handle HTTP error codes: {0}")
        @ValueSource(ints = {400, 401, 403, 404, 500, 502, 503})
        @DisplayName("Should handle various HTTP error codes")
        void should_handleHttpErrorCodes_when_serverReturnsError(int errorCode) throws Exception {
            // Given
            ErrorResponse errorResponse = createMockErrorResponse(
                "ERROR_" + errorCode,
                "Server error occurred",
                "HTTP " + errorCode + " error details"
            );
            stubFor(get(urlEqualTo("/cursors?page=1&limit=10"))
                .willReturn(aResponse()
                    .withStatus(errorCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(errorResponse))));

            // When & Then
            assertThatThrownBy(() -> cursorsApi.listCursors(1, 10))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getCode())
                .isEqualTo(errorCode);
        }
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
