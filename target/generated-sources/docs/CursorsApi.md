# CursorsApi

All URIs are relative to *http://localhost:8080/api/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createCursor**](CursorsApi.md#createCursor) | **POST** /cursors | Create a new cursor |
| [**deleteCursor**](CursorsApi.md#deleteCursor) | **DELETE** /cursors/{cursorId} | Delete cursor |
| [**getCursorById**](CursorsApi.md#getCursorById) | **GET** /cursors/{cursorId} | Get cursor by ID |
| [**listCursors**](CursorsApi.md#listCursors) | **GET** /cursors | List all cursors |
| [**moveCursor**](CursorsApi.md#moveCursor) | **POST** /cursors/{cursorId}/move | Move cursor |
| [**updateCursor**](CursorsApi.md#updateCursor) | **PUT** /cursors/{cursorId} | Update cursor |


<a id="createCursor"></a>
# **createCursor**
> Cursor createCursor(createCursorRequest)

Create a new cursor

Create a new cursor with specified properties

### Example
```java
// Import classes:
import com.example.client.ApiClient;
import com.example.client.ApiException;
import com.example.client.Configuration;
import com.example.client.auth.*;
import com.example.client.models.*;
import com.example.client.api.CursorsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080/api/v1");
    
    // Configure API key authorization: ApiKeyAuth
    ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
    ApiKeyAuth.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //ApiKeyAuth.setApiKeyPrefix("Token");

    CursorsApi apiInstance = new CursorsApi(defaultClient);
    CreateCursorRequest createCursorRequest = new CreateCursorRequest(); // CreateCursorRequest | 
    try {
      Cursor result = apiInstance.createCursor(createCursorRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling CursorsApi#createCursor");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createCursorRequest** | [**CreateCursorRequest**](CreateCursorRequest.md)|  | |

### Return type

[**Cursor**](Cursor.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Cursor created successfully |  -  |
| **400** | Bad request |  -  |

<a id="deleteCursor"></a>
# **deleteCursor**
> deleteCursor(cursorId)

Delete cursor

Delete a cursor by its unique identifier

### Example
```java
// Import classes:
import com.example.client.ApiClient;
import com.example.client.ApiException;
import com.example.client.Configuration;
import com.example.client.auth.*;
import com.example.client.models.*;
import com.example.client.api.CursorsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080/api/v1");
    
    // Configure API key authorization: ApiKeyAuth
    ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
    ApiKeyAuth.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //ApiKeyAuth.setApiKeyPrefix("Token");

    CursorsApi apiInstance = new CursorsApi(defaultClient);
    String cursorId = "cursor-001"; // String | Unique identifier of the cursor
    try {
      apiInstance.deleteCursor(cursorId);
    } catch (ApiException e) {
      System.err.println("Exception when calling CursorsApi#deleteCursor");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **cursorId** | **String**| Unique identifier of the cursor | |

### Return type

null (empty response body)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Cursor deleted successfully |  -  |
| **404** | Cursor not found |  -  |

<a id="getCursorById"></a>
# **getCursorById**
> Cursor getCursorById(cursorId)

Get cursor by ID

Retrieve a specific cursor by its unique identifier

### Example
```java
// Import classes:
import com.example.client.ApiClient;
import com.example.client.ApiException;
import com.example.client.Configuration;
import com.example.client.auth.*;
import com.example.client.models.*;
import com.example.client.api.CursorsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080/api/v1");
    
    // Configure API key authorization: ApiKeyAuth
    ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
    ApiKeyAuth.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //ApiKeyAuth.setApiKeyPrefix("Token");

    CursorsApi apiInstance = new CursorsApi(defaultClient);
    String cursorId = "cursor-001"; // String | Unique identifier of the cursor
    try {
      Cursor result = apiInstance.getCursorById(cursorId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling CursorsApi#getCursorById");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **cursorId** | **String**| Unique identifier of the cursor | |

### Return type

[**Cursor**](Cursor.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successful response |  -  |
| **404** | Cursor not found |  -  |

<a id="listCursors"></a>
# **listCursors**
> CursorListResponse listCursors(page, limit)

List all cursors

Retrieve a list of all available cursors

### Example
```java
// Import classes:
import com.example.client.ApiClient;
import com.example.client.ApiException;
import com.example.client.Configuration;
import com.example.client.auth.*;
import com.example.client.models.*;
import com.example.client.api.CursorsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080/api/v1");
    
    // Configure API key authorization: ApiKeyAuth
    ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
    ApiKeyAuth.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //ApiKeyAuth.setApiKeyPrefix("Token");

    CursorsApi apiInstance = new CursorsApi(defaultClient);
    Integer page = 1; // Integer | Page number for pagination
    Integer limit = 10; // Integer | Number of items per page
    try {
      CursorListResponse result = apiInstance.listCursors(page, limit);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling CursorsApi#listCursors");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | **Integer**| Page number for pagination | [optional] [default to 1] |
| **limit** | **Integer**| Number of items per page | [optional] [default to 10] |

### Return type

[**CursorListResponse**](CursorListResponse.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successful response |  -  |
| **400** | Bad request |  -  |

<a id="moveCursor"></a>
# **moveCursor**
> Cursor moveCursor(cursorId, moveCursorRequest)

Move cursor

Move a cursor to a new position

### Example
```java
// Import classes:
import com.example.client.ApiClient;
import com.example.client.ApiException;
import com.example.client.Configuration;
import com.example.client.auth.*;
import com.example.client.models.*;
import com.example.client.api.CursorsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080/api/v1");
    
    // Configure API key authorization: ApiKeyAuth
    ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
    ApiKeyAuth.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //ApiKeyAuth.setApiKeyPrefix("Token");

    CursorsApi apiInstance = new CursorsApi(defaultClient);
    String cursorId = "cursor-001"; // String | Unique identifier of the cursor
    MoveCursorRequest moveCursorRequest = new MoveCursorRequest(); // MoveCursorRequest | 
    try {
      Cursor result = apiInstance.moveCursor(cursorId, moveCursorRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling CursorsApi#moveCursor");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **cursorId** | **String**| Unique identifier of the cursor | |
| **moveCursorRequest** | [**MoveCursorRequest**](MoveCursorRequest.md)|  | |

### Return type

[**Cursor**](Cursor.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Cursor moved successfully |  -  |
| **404** | Cursor not found |  -  |

<a id="updateCursor"></a>
# **updateCursor**
> Cursor updateCursor(cursorId, updateCursorRequest)

Update cursor

Update an existing cursor with new properties

### Example
```java
// Import classes:
import com.example.client.ApiClient;
import com.example.client.ApiException;
import com.example.client.Configuration;
import com.example.client.auth.*;
import com.example.client.models.*;
import com.example.client.api.CursorsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080/api/v1");
    
    // Configure API key authorization: ApiKeyAuth
    ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
    ApiKeyAuth.setApiKey("YOUR API KEY");
    // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
    //ApiKeyAuth.setApiKeyPrefix("Token");

    CursorsApi apiInstance = new CursorsApi(defaultClient);
    String cursorId = "cursor-001"; // String | Unique identifier of the cursor
    UpdateCursorRequest updateCursorRequest = new UpdateCursorRequest(); // UpdateCursorRequest | 
    try {
      Cursor result = apiInstance.updateCursor(cursorId, updateCursorRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling CursorsApi#updateCursor");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **cursorId** | **String**| Unique identifier of the cursor | |
| **updateCursorRequest** | [**UpdateCursorRequest**](UpdateCursorRequest.md)|  | |

### Return type

[**Cursor**](Cursor.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Cursor updated successfully |  -  |
| **404** | Cursor not found |  -  |

