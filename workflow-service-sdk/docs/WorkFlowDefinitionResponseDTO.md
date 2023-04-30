

# WorkFlowDefinitionResponseDTO


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**author** | **String** |  |  [optional] |
|**createDate** | **Date** |  |  [optional] |
|**id** | **UUID** |  |  [optional] |
|**modifyDate** | **Date** |  |  [optional] |
|**name** | **String** |  |  [optional] |
|**parameters** | **Map&lt;String, Map&lt;String, Object&gt;&gt;** |  |  [optional] |
|**processingType** | [**ProcessingTypeEnum**](#ProcessingTypeEnum) |  |  [optional] |
|**properties** | [**WorkFlowPropertiesDefinitionDTO**](WorkFlowPropertiesDefinitionDTO.md) |  |  [optional] |
|**type** | **String** |  |  [optional] |
|**works** | [**List&lt;WorkDefinitionResponseDTO&gt;**](WorkDefinitionResponseDTO.md) |  |  [optional] |



## Enum: ProcessingTypeEnum

| Name | Value |
|---- | -----|
| SEQUENTIAL | &quot;SEQUENTIAL&quot; |
| PARALLEL | &quot;PARALLEL&quot; |
| OTHER | &quot;OTHER&quot; |



