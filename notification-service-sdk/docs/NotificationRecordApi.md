# NotificationRecordApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**countUnreadNotifications**](NotificationRecordApi.md#countUnreadNotifications) | **GET** /api/v1/notifications/count | 
[**deleteNotification**](NotificationRecordApi.md#deleteNotification) | **DELETE** /api/v1/notifications/{id} | 
[**getNotifications**](NotificationRecordApi.md#getNotifications) | **GET** /api/v1/notifications | 
[**updateNotificationStatusById**](NotificationRecordApi.md#updateNotificationStatusById) | **PUT** /api/v1/notifications/{id} | 


<a name="countUnreadNotifications"></a>
# **countUnreadNotifications**
> Integer countUnreadNotifications(state)



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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **state** | **String**|  | [enum: ARCHIVED, UNREAD]

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
**200** | OK |  -  |

<a name="deleteNotification"></a>
# **deleteNotification**
> deleteNotification(id)



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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**204** | No Content |  -  |

<a name="getNotifications"></a>
# **getNotifications**
> PagedModelNotificationRecordResponseDTO getNotifications(pageable, state, searchTerm)



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
    Pageable pageable = new Pageable(); // Pageable | 
    String state = "ARCHIVED"; // String | 
    String searchTerm = "searchTerm_example"; // String | 
    try {
      PagedModelNotificationRecordResponseDTO result = apiInstance.getNotifications(pageable, state, searchTerm);
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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **pageable** | [**Pageable**](.md)|  |
 **state** | **String**|  | [optional] [enum: ARCHIVED, UNREAD]
 **searchTerm** | **String**|  | [optional]

### Return type

[**PagedModelNotificationRecordResponseDTO**](PagedModelNotificationRecordResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |

<a name="updateNotificationStatusById"></a>
# **updateNotificationStatusById**
> NotificationRecordResponseDTO updateNotificationStatusById(id, operation)



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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  |
 **operation** | **String**|  | [enum: ARCHIVE, READ]

### Return type

[**NotificationRecordResponseDTO**](NotificationRecordResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | OK |  -  |

