

# WorkDefinitionResponseDTO


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**author** | **String** |  |  [optional] |
|**cronExpression** | **String** |  |  [optional] |
|**id** | **UUID** |  |  [optional] |
|**name** | **String** |  |  [optional] |
|**outputs** | [**List&lt;OutputsEnum&gt;**](#List&lt;OutputsEnum&gt;) |  |  [optional] |
|**parameters** | **Map&lt;String, Map&lt;String, Object&gt;&gt;** |  |  [optional] |
|**processingType** | [**ProcessingTypeEnum**](#ProcessingTypeEnum) |  |  [optional] |
|**workFlowCheckerMappingDefinitionId** | **UUID** |  |  [optional] |
|**workType** | [**WorkTypeEnum**](#WorkTypeEnum) |  |  [optional] |
|**works** | [**Set&lt;WorkDefinitionResponseDTO&gt;**](WorkDefinitionResponseDTO.md) |  |  [optional] |



## Enum: List&lt;OutputsEnum&gt;

| Name | Value |
|---- | -----|
| EXCEPTION | &quot;EXCEPTION&quot; |
| HTTP2XX | &quot;HTTP2XX&quot; |
| NO_EXCEPTION | &quot;NO_EXCEPTION&quot; |
| OTHER | &quot;OTHER&quot; |



## Enum: ProcessingTypeEnum

| Name | Value |
|---- | -----|
| SEQUENTIAL | &quot;SEQUENTIAL&quot; |
| PARALLEL | &quot;PARALLEL&quot; |
| OTHER | &quot;OTHER&quot; |



## Enum: WorkTypeEnum

| Name | Value |
|---- | -----|
| TASK | &quot;TASK&quot; |
| WORKFLOW | &quot;WORKFLOW&quot; |
| CHECKER | &quot;CHECKER&quot; |



