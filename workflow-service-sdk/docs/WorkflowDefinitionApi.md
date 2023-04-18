# WorkflowDefinitionApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getWorkFlowDefinitionById**](WorkflowDefinitionApi.md#getWorkFlowDefinitionById) | **GET** /api/v1/workflowdefinitions/{id} | Returns information about a workflow definition by id
[**getWorkFlowDefinitions**](WorkflowDefinitionApi.md#getWorkFlowDefinitions) | **GET** /api/v1/workflowdefinitions | Returns a list of workflow definition


<a name="getWorkFlowDefinitionById"></a>
# **getWorkFlowDefinitionById**
> WorkFlowDefinitionResponseDTO getWorkFlowDefinitionById(id)

Returns information about a workflow definition by id

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowDefinitionApi apiInstance = new WorkflowDefinitionApi(defaultClient);
    String id = "id_example"; // String | 
    try {
      WorkFlowDefinitionResponseDTO result = apiInstance.getWorkFlowDefinitionById(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowDefinitionApi#getWorkFlowDefinitionById");
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
 **id** | **String**|  |

### Return type

[**WorkFlowDefinitionResponseDTO**](WorkFlowDefinitionResponseDTO.md)

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
**404** | Not found |  -  |

<a name="getWorkFlowDefinitions"></a>
# **getWorkFlowDefinitions**
> List&lt;WorkFlowDefinitionResponseDTO&gt; getWorkFlowDefinitions(name)

Returns a list of workflow definition

### Example
```java
// Import classes:
import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.invoker.Configuration;
import com.redhat.parodos.sdk.invoker.models.*;
import com.redhat.parodos.sdk.api.WorkflowDefinitionApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    WorkflowDefinitionApi apiInstance = new WorkflowDefinitionApi(defaultClient);
    String name = "name_example"; // String | 
    try {
      List<WorkFlowDefinitionResponseDTO> result = apiInstance.getWorkFlowDefinitions(name);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowDefinitionApi#getWorkFlowDefinitions");
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
 **name** | **String**|  | [optional]

### Return type

[**List&lt;WorkFlowDefinitionResponseDTO&gt;**](WorkFlowDefinitionResponseDTO.md)

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

