# ProjectAccessApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getProjectAccessStatus**](ProjectAccessApi.md#getProjectAccessStatus) | **GET** /api/v1/projects/access/{id}/status | Returns status about a specified project access request |
| [**updateProjectAccessStatus**](ProjectAccessApi.md#updateProjectAccessStatus) | **POST** /api/v1/projects/access/{id}/status | Update status of a specified project access request |


<a id="getProjectAccessStatus"></a>
# **getProjectAccessStatus**
> AccessStatusResponseDTO getProjectAccessStatus(id)

Returns status about a specified project access request

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectAccessApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectAccessApi apiInstance = new ProjectAccessApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    try {
      AccessStatusResponseDTO result = apiInstance.getProjectAccessStatus(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectAccessApi#getProjectAccessStatus");
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

[**AccessStatusResponseDTO**](AccessStatusResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Succeeded |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not found |  -  |
| **409** | Conflict |  -  |

<a id="updateProjectAccessStatus"></a>
# **updateProjectAccessStatus**
> updateProjectAccessStatus(id, accessStatusRequestDTO)

Update status of a specified project access request

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectAccessApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectAccessApi apiInstance = new ProjectAccessApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    AccessStatusRequestDTO accessStatusRequestDTO = new AccessStatusRequestDTO(); // AccessStatusRequestDTO | 
    try {
      apiInstance.updateProjectAccessStatus(id, accessStatusRequestDTO);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectAccessApi#updateProjectAccessStatus");
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
| **accessStatusRequestDTO** | [**AccessStatusRequestDTO**](AccessStatusRequestDTO.md)|  | |

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
| **204** | Updated successfully |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not found |  -  |
| **409** | Conflict |  -  |

