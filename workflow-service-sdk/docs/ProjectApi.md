# ProjectApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createProject**](ProjectApi.md#createProject) | **POST** /api/v1/projects | Creates a new project |
| [**getProjectById**](ProjectApi.md#getProjectById) | **GET** /api/v1/projects/{id} | Returns information about a specified project |
| [**getProjects**](ProjectApi.md#getProjects) | **GET** /api/v1/projects | Returns a list of project |
| [**removeUsersFromProject**](ProjectApi.md#removeUsersFromProject) | **DELETE** /api/v1/projects/{id}/users | Remove users from project |
| [**updateUserRolesToProject**](ProjectApi.md#updateUserRolesToProject) | **POST** /api/v1/projects/{id}/users | Update user roles in project |


<a name="createProject"></a>
# **createProject**
> ProjectResponseDTO createProject(projectRequestDTO)

Creates a new project

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    ProjectRequestDTO projectRequestDTO = new ProjectRequestDTO(); // ProjectRequestDTO | 
    try {
      ProjectResponseDTO result = apiInstance.createProject(projectRequestDTO);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#createProject");
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
| **projectRequestDTO** | [**ProjectRequestDTO**](ProjectRequestDTO.md)|  | |

### Return type

[**ProjectResponseDTO**](ProjectResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Created |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |

<a name="getProjectById"></a>
# **getProjectById**
> ProjectResponseDTO getProjectById(id)

Returns information about a specified project

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    try {
      ProjectResponseDTO result = apiInstance.getProjectById(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#getProjectById");
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

[**ProjectResponseDTO**](ProjectResponseDTO.md)

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

<a name="getProjects"></a>
# **getProjects**
> List&lt;ProjectResponseDTO&gt; getProjects()

Returns a list of project

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    try {
      List<ProjectResponseDTO> result = apiInstance.getProjects();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#getProjects");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;ProjectResponseDTO&gt;**](ProjectResponseDTO.md)

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
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |

<a name="removeUsersFromProject"></a>
# **removeUsersFromProject**
> ProjectUserRoleResponseDTO removeUsersFromProject(id, requestBody)

Remove users from project

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    List<String> requestBody = Arrays.asList(); // List<String> | 
    try {
      ProjectUserRoleResponseDTO result = apiInstance.removeUsersFromProject(id, requestBody);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#removeUsersFromProject");
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
| **requestBody** | [**List&lt;String&gt;**](String.md)|  | |

### Return type

[**ProjectUserRoleResponseDTO**](ProjectUserRoleResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Succeeded |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not found |  -  |
| **409** | Conflict |  -  |

<a name="updateUserRolesToProject"></a>
# **updateUserRolesToProject**
> ProjectUserRoleResponseDTO updateUserRolesToProject(id, userRoleRequestDTO)

Update user roles in project

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.ProjectApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    ProjectApi apiInstance = new ProjectApi(defaultClient);
    UUID id = UUID.randomUUID(); // UUID | 
    List<UserRoleRequestDTO> userRoleRequestDTO = Arrays.asList(); // List<UserRoleRequestDTO> | 
    try {
      ProjectUserRoleResponseDTO result = apiInstance.updateUserRolesToProject(id, userRoleRequestDTO);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ProjectApi#updateUserRolesToProject");
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
| **userRoleRequestDTO** | [**List&lt;UserRoleRequestDTO&gt;**](UserRoleRequestDTO.md)|  | |

### Return type

[**ProjectUserRoleResponseDTO**](ProjectUserRoleResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Succeeded |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **404** | Not found |  -  |
| **409** | Conflict |  -  |

