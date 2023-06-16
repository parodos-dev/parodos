# WorkflowDefinitionApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getWorkFlowDefinitionById**](WorkflowDefinitionApi.md#getWorkFlowDefinitionById) | **GET** /api/v1/workflowdefinitions/{id} | Returns information about a workflow definition by id |
| [**getWorkFlowDefinitions**](WorkflowDefinitionApi.md#getWorkFlowDefinitions) | **GET** /api/v1/workflowdefinitions | Returns a list of workflow definition |
| [**updateParameter**](WorkflowDefinitionApi.md#updateParameter) | **POST** /api/v1/workflowdefinitions/{workflowDefinitionName}/parameters/update/{valueProviderName} | Returns updated parameter value |


<a id="getWorkFlowDefinitionById"></a>
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
    UUID id = UUID.randomUUID(); // UUID | 
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

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

[**WorkFlowDefinitionResponseDTO**](WorkFlowDefinitionResponseDTO.md)

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
| **500** | Internal Server Error |  -  |

<a id="getWorkFlowDefinitions"></a>
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

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **name** | **String**|  | [optional] |

### Return type

[**List&lt;WorkFlowDefinitionResponseDTO&gt;**](WorkFlowDefinitionResponseDTO.md)

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

<a id="updateParameter"></a>
# **updateParameter**
> List&lt;WorkParameterValueResponseDTO&gt; updateParameter(workflowDefinitionName, valueProviderName, workParameterValueRequestDTO)

Returns updated parameter value

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
    String workflowDefinitionName = "complexWorkFlow"; // String | workflow Definition Name
    String valueProviderName = "complexWorkFlowValueProvider"; // String | valueProvider Name. It can be referenced to 'valueProviderName' in [GET /getWorkFlowDefinitions](#/Workflow%20Definition/getWorkFlowDefinitions)
    List<WorkParameterValueRequestDTO> workParameterValueRequestDTO = Arrays.asList(); // List<WorkParameterValueRequestDTO> | 
    try {
      List<WorkParameterValueResponseDTO> result = apiInstance.updateParameter(workflowDefinitionName, valueProviderName, workParameterValueRequestDTO);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling WorkflowDefinitionApi#updateParameter");
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
| **workflowDefinitionName** | **String**| workflow Definition Name | |
| **valueProviderName** | **String**| valueProvider Name. It can be referenced to &#39;valueProviderName&#39; in [GET /getWorkFlowDefinitions](#/Workflow%20Definition/getWorkFlowDefinitions) | |
| **workParameterValueRequestDTO** | [**List&lt;WorkParameterValueRequestDTO&gt;**](WorkParameterValueRequestDTO.md)|  | |

### Return type

[**List&lt;WorkParameterValueResponseDTO&gt;**](WorkParameterValueResponseDTO.md)

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
| **404** | Not Found |  -  |
| **409** | Conflict |  -  |
| **500** | Internal Server Error |  -  |

