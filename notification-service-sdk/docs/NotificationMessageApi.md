# NotificationMessageApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**create**](NotificationMessageApi.md#create) | **POST** /api/v1/messages |  |


<a name="create"></a>
# **create**
> create(notificationMessageCreateRequestDTO)



### Example
```java
// Import classes:
import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.models.*;
import com.redhat.parodos.notification.sdk.api.NotificationMessageApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    NotificationMessageApi apiInstance = new NotificationMessageApi(defaultClient);
    NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO(); // NotificationMessageCreateRequestDTO | 
    try {
      apiInstance.create(notificationMessageCreateRequestDTO);
    } catch (ApiException e) {
      System.err.println("Exception when calling NotificationMessageApi#create");
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
| **notificationMessageCreateRequestDTO** | [**NotificationMessageCreateRequestDTO**](NotificationMessageCreateRequestDTO.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Created |  -  |
| **400** | Bad Request |  -  |
| **404** | Not Found |  -  |

