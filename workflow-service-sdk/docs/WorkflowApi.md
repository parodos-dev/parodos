# WorkflowApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**execute**](WorkflowApi.md#execute) | **POST** /api/v1/workflows | Executes a workflow |
| [**getLog**](WorkflowApi.md#getLog) | **GET** /api/v1/workflows/{workFlowExecutionId}/log | Returns workflow execution log |
| [**getStatus**](WorkflowApi.md#getStatus) | **GET** /api/v1/workflows/{workFlowExecutionId}/status | Returns a workflow status |
| [**getStatusByProjectId**](WorkflowApi.md#getStatusByProjectId) | **GET** /api/v1/workflows | Returns workflows by project id |
| [**getWorkflowParameters**](WorkflowApi.md#getWorkflowParameters) | **GET** /api/v1/workflows/{workFlowExecutionId}/context | Returns workflow context parameters |
| [**restartWorkFlow**](WorkflowApi.md#restartWorkFlow) | **POST** /api/v1/workflows/{workFlowExecutionId}/restart | Restart a workflow execution with same parameters |
| [**updateWorkFlowCheckerTaskStatus**](WorkflowApi.md#updateWorkFlowCheckerTaskStatus) | **POST** /api/v1/workflows/{workFlowExecutionId}/checkers/{workFlowCheckerTaskName} | Updates a workflow checker task status |


<a id="execute"></a>
# **execute**
> WorkFlowExecutionResponseDTO execute(workFlowRequestDTO)

Executes a workflow

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    WorkFlowRequestDTO workFlowRequestDTO = new WorkFlowRequestDTO(); // WorkFlowRequestDTO | 
    try {
      WorkFlowExecutionResponseDTO result = apiInstance.execute(workFlowRequestDTO);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#execute");
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
| **workFlowRequestDTO** | [**WorkFlowRequestDTO**](WorkFlowRequestDTO.md)|  | |

### Return type

[**WorkFlowExecutionResponseDTO**](WorkFlowExecutionResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **202** | Accepted |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |

<a id="getLog"></a>
# **getLog**
> String getLog(workFlowExecutionId, taskName)

Returns workflow execution log

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    UUID workFlowExecutionId = UUID.randomUUID(); // UUID | 
    String taskName = "taskName_example"; // String | 
    try {
      String result = apiInstance.getLog(workFlowExecutionId, taskName);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#getLog");
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
| **workFlowExecutionId** | **UUID**|  | |
| **taskName** | **String**|  | [optional] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Succeeded |  -  |
| **304** | Not Modified |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |

<a id="getStatus"></a>
# **getStatus**
> WorkFlowStatusResponseDTO getStatus(workFlowExecutionId)

Returns a workflow status

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    UUID workFlowExecutionId = UUID.randomUUID(); // UUID | 
    try {
      WorkFlowStatusResponseDTO result = apiInstance.getStatus(workFlowExecutionId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#getStatus");
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
| **workFlowExecutionId** | **UUID**|  | |

### Return type

[**WorkFlowStatusResponseDTO**](WorkFlowStatusResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Succeeded |  -  |
| **304** | Not Modified |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |

<a id="getStatusByProjectId"></a>
# **getStatusByProjectId**
> List&lt;WorkFlowResponseDTO&gt; getStatusByProjectId(projectId)

Returns workflows by project id

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    UUID projectId = UUID.randomUUID(); // UUID | 
    try {
      List<WorkFlowResponseDTO> result = apiInstance.getStatusByProjectId(projectId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#getStatusByProjectId");
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
| **projectId** | **UUID**|  | [optional] |

### Return type

[**List&lt;WorkFlowResponseDTO&gt;**](WorkFlowResponseDTO.md)

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
| **500** | Internal Server Error |  -  |

<a id="getWorkflowParameters"></a>
# **getWorkflowParameters**
> WorkFlowContextResponseDTO getWorkflowParameters(workFlowExecutionId, param)

Returns workflow context parameters

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    UUID workFlowExecutionId = UUID.randomUUID(); // UUID | 
    List<String> param = Arrays.asList(); // List<String> | 
    try {
      WorkFlowContextResponseDTO result = apiInstance.getWorkflowParameters(workFlowExecutionId, param);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#getWorkflowParameters");
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
| **workFlowExecutionId** | **UUID**|  | |
| **param** | [**List&lt;String&gt;**](String.md)|  | [enum: ID, NAME, PARAMETERS, ARGUMENTS, STATUS, WORKFLOW_OPTIONS, PARENT_WORKFLOW, ADDITIONAL_INFO] |

### Return type

[**WorkFlowContextResponseDTO**](WorkFlowContextResponseDTO.md)

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
| **500** | Internal Server Error |  -  |

<a id="restartWorkFlow"></a>
# **restartWorkFlow**
> WorkFlowExecutionResponseDTO restartWorkFlow(workFlowExecutionId)

Restart a workflow execution with same parameters

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    UUID workFlowExecutionId = UUID.randomUUID(); // UUID | 
    try {
      WorkFlowExecutionResponseDTO result = apiInstance.restartWorkFlow(workFlowExecutionId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#restartWorkFlow");
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
| **workFlowExecutionId** | **UUID**|  | |

### Return type

[**WorkFlowExecutionResponseDTO**](WorkFlowExecutionResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json, */*

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **202** | Accepted |  -  |
| **400** | Bad Request |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |

<a id="updateWorkFlowCheckerTaskStatus"></a>
# **updateWorkFlowCheckerTaskStatus**
> String updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName, workFlowCheckerTaskRequestDTO)

Updates a workflow checker task status

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowApi apiInstance = new WorkflowApi(defaultClient);
    UUID workFlowExecutionId = UUID.randomUUID(); // UUID | 
    String workFlowCheckerTaskName = "workFlowCheckerTaskName_example"; // String | 
    WorkFlowCheckerTaskRequestDTO workFlowCheckerTaskRequestDTO = new WorkFlowCheckerTaskRequestDTO(); // WorkFlowCheckerTaskRequestDTO | 
    try {
      String result = apiInstance.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName, workFlowCheckerTaskRequestDTO);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowApi#updateWorkFlowCheckerTaskStatus");
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
| **workFlowExecutionId** | **UUID**|  | |
| **workFlowCheckerTaskName** | **String**|  | |
| **workFlowCheckerTaskRequestDTO** | [**WorkFlowCheckerTaskRequestDTO**](WorkFlowCheckerTaskRequestDTO.md)|  | |

### Return type

**String**

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
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |

