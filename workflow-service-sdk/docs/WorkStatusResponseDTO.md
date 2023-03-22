

# WorkStatusResponseDTO


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **String** |  |  [optional]
**type** | [**TypeEnum**](#TypeEnum) |  |  [optional]
**status** | [**StatusEnum**](#StatusEnum) |  |  [optional]
**works** | [**List&lt;WorkStatusResponseDTO&gt;**](WorkStatusResponseDTO.md) |  |  [optional]



## Enum: TypeEnum

Name | Value
---- | -----
TASK | &quot;TASK&quot;
WORKFLOW | &quot;WORKFLOW&quot;



## Enum: StatusEnum

Name | Value
---- | -----
FAILED | &quot;FAILED&quot;
COMPLETED | &quot;COMPLETED&quot;
PENDING | &quot;PENDING&quot;



