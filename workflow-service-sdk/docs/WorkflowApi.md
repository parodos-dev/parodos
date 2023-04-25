# WorkflowApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**execute**](WorkflowApi.md#execute) | **POST** /api/v1/workflows | Executes a workflow
[**getStatus**](WorkflowApi.md#getStatus) | **GET** /api/v1/workflows/{workFlowExecutionId}/status | Returns a workflow status
[**getWorkflowParameters**](WorkflowApi.md#getWorkflowParameters) | **GET** /api/v1/workflows/{workFlowExecutionId}/context | Returns workflow context parameters
[**updateWorkFlowCheckerTaskStatus**](WorkflowApi.md#updateWorkFlowCheckerTaskStatus) | **POST** /api/v1/workflows/{workFlowExecutionId}/checkers/{workFlowCheckerTaskName} | Updates a workflow checker task status


<a name="execute"></a>
# **execute**
> WorkFlowResponseDTO execute(workFlowRequestDTO)

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
      WorkFlowResponseDTO result = apiInstance.execute(workFlowRequestDTO);
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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **workFlowRequestDTO** | [**WorkFlowRequestDTO**](WorkFlowRequestDTO.md)|  |

### Return type

[**WorkFlowResponseDTO**](WorkFlowResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Succeeded |  -  |
**401** | Unauthorized |  -  |
**403** | Forbidden |  -  |

<a name="getStatus"></a>
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
    String workFlowExecutionId = "workFlowExecutionId_example"; // String | 
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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **workFlowExecutionId** | **String**|  |

### Return type

[**WorkFlowStatusResponseDTO**](WorkFlowStatusResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Succeeded |  -  |
**401** | Unauthorized |  -  |
**403** | Forbidden |  -  |

<a name="getWorkflowParameters"></a>
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
    String workFlowExecutionId = "workFlowExecutionId_example"; // String | 
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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **workFlowExecutionId** | **String**|  |
 **param** | [**List&lt;String&gt;**](String.md)|  | [enum: ID, NAME, PARAMETERS, ARGUMENTS, STATUS, WORKFLOW_OPTIONS, PARENT_WORKFLOW]

### Return type

[**WorkFlowContextResponseDTO**](WorkFlowContextResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Succeeded |  -  |
**401** | Unauthorized |  -  |
**403** | Forbidden |  -  |

<a name="updateWorkFlowCheckerTaskStatus"></a>
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
    String workFlowExecutionId = "workFlowExecutionId_example"; // String | 
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

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **workFlowExecutionId** | **String**|  |
 **workFlowCheckerTaskName** | **String**|  |
 **workFlowCheckerTaskRequestDTO** | [**WorkFlowCheckerTaskRequestDTO**](WorkFlowCheckerTaskRequestDTO.md)|  |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Succeeded |  -  |
**401** | Unauthorized |  -  |
**403** | Forbidden |  -  |
**404** | Not found |  -  |

