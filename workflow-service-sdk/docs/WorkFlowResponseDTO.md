

# WorkFlowResponseDTO


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**additionalInfos** | [**List&lt;AdditionalInfo&gt;**](AdditionalInfo.md) |  |  [optional] |
|**endDate** | **String** |  |  [optional] |
|**executeBy** | **String** |  |  [optional] |
|**projectId** | **UUID** |  |  [optional] |
|**startDate** | **String** |  |  [optional] |
|**workFlowExecutionId** | **UUID** |  |  [optional] |
|**workFlowName** | **String** |  |  [optional] |
|**workStatus** | [**WorkStatusEnum**](#WorkStatusEnum) |  |  [optional] |



## Enum: WorkStatusEnum

| Name | Value |
|---- | -----|
| FAILED | &quot;FAILED&quot; |
| COMPLETED | &quot;COMPLETED&quot; |
| IN_PROGRESS | &quot;IN_PROGRESS&quot; |
| REJECTED | &quot;REJECTED&quot; |
| PENDING | &quot;PENDING&quot; |



