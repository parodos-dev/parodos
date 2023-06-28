

# WorkStatusResponseDTO


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**alertMessage** | **String** |  |  [optional] |
|**message** | **String** |  |  [optional] |
|**name** | **String** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**type** | [**TypeEnum**](#TypeEnum) |  |  [optional] |
|**works** | [**List&lt;WorkStatusResponseDTO&gt;**](WorkStatusResponseDTO.md) |  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| FAILED | &quot;FAILED&quot; |
| COMPLETED | &quot;COMPLETED&quot; |
| IN_PROGRESS | &quot;IN_PROGRESS&quot; |
| REJECTED | &quot;REJECTED&quot; |
| PENDING | &quot;PENDING&quot; |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| TASK | &quot;TASK&quot; |
| WORKFLOW | &quot;WORKFLOW&quot; |
| CHECKER | &quot;CHECKER&quot; |



