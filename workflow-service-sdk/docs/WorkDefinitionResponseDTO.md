

# WorkDefinitionResponseDTO


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**author** | **String** |  |  [optional]
**id** | **String** |  |  [optional]
**name** | **String** |  |  [optional]
**outputs** | [**List&lt;OutputsEnum&gt;**](#List&lt;OutputsEnum&gt;) |  |  [optional]
**parameters** | **Map&lt;String, Map&lt;String, String&gt;&gt;** |  |  [optional]
**processingType** | **String** |  |  [optional]
**workType** | **String** |  |  [optional]
**works** | [**List&lt;WorkDefinitionResponseDTO&gt;**](WorkDefinitionResponseDTO.md) |  |  [optional]



## Enum: List&lt;OutputsEnum&gt;

Name | Value
---- | -----
EXCEPTION | &quot;EXCEPTION&quot;
HTTP2XX | &quot;HTTP2XX&quot;
NO_EXCEPTION | &quot;NO_EXCEPTION&quot;
OTHER | &quot;OTHER&quot;



