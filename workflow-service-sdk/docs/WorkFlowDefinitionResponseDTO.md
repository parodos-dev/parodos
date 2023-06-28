

# WorkFlowDefinitionResponseDTO


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**author** | **String** |  |  [optional] |
|**createDate** | **Date** |  |  [optional] |
|**cronExpression** | **String** |  |  [optional] |
|**fallbackWorkflow** | **String** |  |  [optional] |
|**id** | **UUID** |  |  [optional] |
|**modifyDate** | **Date** |  |  [optional] |
|**name** | **String** |  |  [optional] |
|**parameters** | **Map&lt;String, Map&lt;String, Object&gt;&gt;** |  |  [optional] |
|**processingType** | [**ProcessingTypeEnum**](#ProcessingTypeEnum) |  |  [optional] |
|**properties** | [**WorkFlowPropertiesDefinitionDTO**](WorkFlowPropertiesDefinitionDTO.md) |  |  [optional] |
|**type** | [**TypeEnum**](#TypeEnum) |  |  [optional] |
|**works** | [**Set&lt;WorkDefinitionResponseDTO&gt;**](WorkDefinitionResponseDTO.md) |  |  [optional] |



## Enum: ProcessingTypeEnum

| Name | Value |
|---- | -----|
| SEQUENTIAL | &quot;SEQUENTIAL&quot; |
| PARALLEL | &quot;PARALLEL&quot; |
| OTHER | &quot;OTHER&quot; |



## Enum: TypeEnum

| Name | Value |
|---- | -----|
| ASSESSMENT | &quot;ASSESSMENT&quot; |
| CHECKER | &quot;CHECKER&quot; |
| INFRASTRUCTURE | &quot;INFRASTRUCTURE&quot; |
| ESCALATION | &quot;ESCALATION&quot; |



