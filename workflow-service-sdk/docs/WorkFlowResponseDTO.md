

# WorkFlowResponseDTO


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**workFlowExecutionId** | **UUID** |  |  [optional] |
|**workFlowOptions** | [**WorkFlowOptions**](WorkFlowOptions.md) |  |  [optional] |
|**workStatus** | [**WorkStatusEnum**](#WorkStatusEnum) |  |  [optional] |



## Enum: WorkStatusEnum

| Name | Value |
|---- | -----|
| FAILED | &quot;FAILED&quot; |
| COMPLETED | &quot;COMPLETED&quot; |
| IN_PROGRESS | &quot;IN_PROGRESS&quot; |
| REJECTED | &quot;REJECTED&quot; |
| PENDING | &quot;PENDING&quot; |



