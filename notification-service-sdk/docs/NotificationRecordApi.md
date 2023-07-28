# NotificationRecordApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**countUnreadNotifications**](NotificationRecordApi.md#countUnreadNotifications) | **GET** /api/v1/notifications/count | Return the number of the notification records with given state for the user |
| [**deleteNotification**](NotificationRecordApi.md#deleteNotification) | **DELETE** /api/v1/notifications/{id} | Delete the specified notification record |
| [**getNotifications**](NotificationRecordApi.md#getNotifications) | **GET** /api/v1/notifications | Return a list of notification records for the user |
| [**updateNotificationStatusById**](NotificationRecordApi.md#updateNotificationStatusById) | **PUT** /api/v1/notifications/{id} | Update the specified notification record with user operation |


<a id="countUnreadNotifications"></a>
# **countUnreadNotifications**
> Integer countUnreadNotifications(state)

Return the number of the notification records with given state for the user

### Example
```java
// Import classes:
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.models.*;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    NotificationRecordApi apiInstance = new NotificationRecordApi(defaultClient);
    String state = "ARCHIVED"; // String | 
    try {
      Integer result = apiInstance.countUnreadNotifications(state);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling NotificationRecordApi#countUnreadNotifications");
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
| **state** | **String**|  | [enum: ARCHIVED, UNREAD] |

### Return type

**Integer**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved the amount of notifications |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not Found |  -  |

<a id="deleteNotification"></a>
# **deleteNotification**
> deleteNotification(id)

Delete the specified notification record

### Example
```java
// Import classes:
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.models.*;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    NotificationRecordApi apiInstance = new NotificationRecordApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    try {
      apiInstance.deleteNotification(id);
    } catch (ApiException e) {
      System.err.println("Exception when calling NotificationRecordApi#deleteNotification");
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
| **id** | **UUID**|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Successfully Deleted |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not Found |  -  |

<a id="getNotifications"></a>
# **getNotifications**
> PageNotificationRecordResponseDTO getNotifications(page, size, sort, state, searchTerm)

Return a list of notification records for the user

### Example
```java
// Import classes:
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.models.*;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    NotificationRecordApi apiInstance = new NotificationRecordApi(defaultClient);
    Integer page = 0; // Integer | Zero-based page index (0..N)
    Integer size = 100; // Integer | The size of the page to be returned
    List<String> sort = Arrays.asList(); // List<String> | Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
    String state = "ARCHIVED"; // String | 
    String searchTerm = "searchTerm_example"; // String | 
    try {
      PageNotificationRecordResponseDTO result = apiInstance.getNotifications(page, size, sort, state, searchTerm);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling NotificationRecordApi#getNotifications");
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
| **page** | **Integer**| Zero-based page index (0..N) | [optional] [default to 0] |
| **size** | **Integer**| The size of the page to be returned | [optional] [default to 100] |
| **sort** | [**List&lt;String&gt;**](String.md)| Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported. | [optional] |
| **state** | **String**|  | [optional] [enum: ARCHIVED, UNREAD] |
| **searchTerm** | **String**|  | [optional] |

### Return type

[**PageNotificationRecordResponseDTO**](PageNotificationRecordResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved page of notifications |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not Found |  -  |

<a id="updateNotificationStatusById"></a>
# **updateNotificationStatusById**
> NotificationRecordResponseDTO updateNotificationStatusById(id, operation)

Update the specified notification record with user operation

### Example
```java
// Import classes:
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.models.*;
import com.redhat.parodos.notification.sdk.api.NotificationRecordApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    NotificationRecordApi apiInstance = new NotificationRecordApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    String operation = "ARCHIVE"; // String | 
    try {
      NotificationRecordResponseDTO result = apiInstance.updateNotificationStatusById(id, operation);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling NotificationRecordApi#updateNotificationStatusById");
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
| **id** | **UUID**|  | |
| **operation** | **String**|  | [enum: ARCHIVE, READ] |

### Return type

[**NotificationRecordResponseDTO**](NotificationRecordResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not found |  -  |

