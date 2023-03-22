

# WorkDefinitionResponseDTO


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** |  |  [optional]
**name** | **String** |  |  [optional]
**workType** | **String** |  |  [optional]
**processingType** | **String** |  |  [optional]
**author** | **String** |  |  [optional]
**works** | [**List&lt;WorkDefinitionResponseDTO&gt;**](WorkDefinitionResponseDTO.md) |  |  [optional]
**parameters** | **List&lt;Object&gt;** |  |  [optional]
**outputs** | [**List&lt;OutputsEnum&gt;**](#List&lt;OutputsEnum&gt;) |  |  [optional]



## Enum: List&lt;OutputsEnum&gt;

Name | Value
---- | -----
EXCEPTION | &quot;EXCEPTION&quot;
HTTP2XX | &quot;HTTP2XX&quot;
NO_EXCEPTION | &quot;NO_EXCEPTION&quot;
OTHER | &quot;OTHER&quot;



