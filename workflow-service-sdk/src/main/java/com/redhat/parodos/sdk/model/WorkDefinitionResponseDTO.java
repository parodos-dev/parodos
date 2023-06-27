/*
 * Parodos Workflow Service API
 * This is the API documentation for the Parodos Workflow Service. It provides operations to execute assessments to determine infrastructure options (tooling + environments). Also executes infrastructure task workflows to call downstream systems to stand-up an infrastructure option.
 *
 * The version of the OpenAPI document: v1.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.redhat.parodos.sdk.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.parodos.sdk.invoker.JSON;

/**
 * WorkDefinitionResponseDTO
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class WorkDefinitionResponseDTO {

	public static final String SERIALIZED_NAME_AUTHOR = "author";

	@SerializedName(SERIALIZED_NAME_AUTHOR)
	private String author;

	public static final String SERIALIZED_NAME_ID = "id";

	@SerializedName(SERIALIZED_NAME_ID)
	private UUID id;

	public static final String SERIALIZED_NAME_NAME = "name";

	@SerializedName(SERIALIZED_NAME_NAME)
	private String name;

	/**
	 * Gets or Sets outputs
	 */
	@JsonAdapter(OutputsEnum.Adapter.class)
	public enum OutputsEnum {

		EXCEPTION("EXCEPTION"),

		HTTP2XX("HTTP2XX"),

		NO_EXCEPTION("NO_EXCEPTION"),

		OTHER("OTHER");

		private String value;

		OutputsEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static OutputsEnum fromValue(String value) {
			for (OutputsEnum b : OutputsEnum.values()) {
				if (b.value.equals(value)) {
					return b;
				}
			}
			throw new IllegalArgumentException("Unexpected value '" + value + "'");
		}

		public static class Adapter extends TypeAdapter<OutputsEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final OutputsEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public OutputsEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return OutputsEnum.fromValue(value);
			}

		}

	}

	public static final String SERIALIZED_NAME_OUTPUTS = "outputs";

	@SerializedName(SERIALIZED_NAME_OUTPUTS)
	private List<OutputsEnum> outputs;

	public static final String SERIALIZED_NAME_PARAMETERS = "parameters";

	@SerializedName(SERIALIZED_NAME_PARAMETERS)
	private Map<String, Map<String, Object>> parameters = new HashMap<>();

	/**
	 * Gets or Sets processingType
	 */
	@JsonAdapter(ProcessingTypeEnum.Adapter.class)
	public enum ProcessingTypeEnum {

		SEQUENTIAL("SEQUENTIAL"),

		PARALLEL("PARALLEL"),

		OTHER("OTHER");

		private String value;

		ProcessingTypeEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static ProcessingTypeEnum fromValue(String value) {
			for (ProcessingTypeEnum b : ProcessingTypeEnum.values()) {
				if (b.value.equals(value)) {
					return b;
				}
			}
			throw new IllegalArgumentException("Unexpected value '" + value + "'");
		}

		public static class Adapter extends TypeAdapter<ProcessingTypeEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final ProcessingTypeEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public ProcessingTypeEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return ProcessingTypeEnum.fromValue(value);
			}

		}

	}

	public static final String SERIALIZED_NAME_PROCESSING_TYPE = "processingType";

	@SerializedName(SERIALIZED_NAME_PROCESSING_TYPE)
	private ProcessingTypeEnum processingType;

	/**
	 * Gets or Sets workType
	 */
	@JsonAdapter(WorkTypeEnum.Adapter.class)
	public enum WorkTypeEnum {

		TASK("TASK"),

		WORKFLOW("WORKFLOW");

		private String value;

		WorkTypeEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static WorkTypeEnum fromValue(String value) {
			for (WorkTypeEnum b : WorkTypeEnum.values()) {
				if (b.value.equals(value)) {
					return b;
				}
			}
			throw new IllegalArgumentException("Unexpected value '" + value + "'");
		}

		public static class Adapter extends TypeAdapter<WorkTypeEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final WorkTypeEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public WorkTypeEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return WorkTypeEnum.fromValue(value);
			}

		}

	}

	public static final String SERIALIZED_NAME_WORK_TYPE = "workType";

	@SerializedName(SERIALIZED_NAME_WORK_TYPE)
	private WorkTypeEnum workType;

	public static final String SERIALIZED_NAME_WORKS = "works";

	@SerializedName(SERIALIZED_NAME_WORKS)
	private List<WorkDefinitionResponseDTO> works;

	public WorkDefinitionResponseDTO() {
	}

	public WorkDefinitionResponseDTO author(String author) {

		this.author = author;
		return this;
	}

	/**
	 * Get author
	 * @return author
	 **/
	@javax.annotation.Nullable
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public WorkDefinitionResponseDTO id(UUID id) {

		this.id = id;
		return this;
	}

	/**
	 * Get id
	 * @return id
	 **/
	@javax.annotation.Nullable
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public WorkDefinitionResponseDTO name(String name) {

		this.name = name;
		return this;
	}

	/**
	 * Get name
	 * @return name
	 **/
	@javax.annotation.Nullable
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public WorkDefinitionResponseDTO outputs(List<OutputsEnum> outputs) {

		this.outputs = outputs;
		return this;
	}

	public WorkDefinitionResponseDTO addOutputsItem(OutputsEnum outputsItem) {
		if (this.outputs == null) {
			this.outputs = new ArrayList<>();
		}
		this.outputs.add(outputsItem);
		return this;
	}

	/**
	 * Get outputs
	 * @return outputs
	 **/
	@javax.annotation.Nullable
	public List<OutputsEnum> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<OutputsEnum> outputs) {
		this.outputs = outputs;
	}

	public WorkDefinitionResponseDTO parameters(Map<String, Map<String, Object>> parameters) {

		this.parameters = parameters;
		return this;
	}

	public WorkDefinitionResponseDTO putParametersItem(String key, Map<String, Object> parametersItem) {
		if (this.parameters == null) {
			this.parameters = new HashMap<>();
		}
		this.parameters.put(key, parametersItem);
		return this;
	}

	/**
	 * Get parameters
	 * @return parameters
	 **/
	@javax.annotation.Nullable
	public Map<String, Map<String, Object>> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Map<String, Object>> parameters) {
		this.parameters = parameters;
	}

	public WorkDefinitionResponseDTO processingType(ProcessingTypeEnum processingType) {

		this.processingType = processingType;
		return this;
	}

	/**
	 * Get processingType
	 * @return processingType
	 **/
	@javax.annotation.Nullable
	public ProcessingTypeEnum getProcessingType() {
		return processingType;
	}

	public void setProcessingType(ProcessingTypeEnum processingType) {
		this.processingType = processingType;
	}

	public WorkDefinitionResponseDTO workType(WorkTypeEnum workType) {

		this.workType = workType;
		return this;
	}

	/**
	 * Get workType
	 * @return workType
	 **/
	@javax.annotation.Nullable
	public WorkTypeEnum getWorkType() {
		return workType;
	}

	public void setWorkType(WorkTypeEnum workType) {
		this.workType = workType;
	}

	public WorkDefinitionResponseDTO works(List<WorkDefinitionResponseDTO> works) {

		this.works = works;
		return this;
	}

	public WorkDefinitionResponseDTO addWorksItem(WorkDefinitionResponseDTO worksItem) {
		if (this.works == null) {
			this.works = new ArrayList<>();
		}
		this.works.add(worksItem);
		return this;
	}

	/**
	 * Get works
	 * @return works
	 **/
	@javax.annotation.Nullable
	public List<WorkDefinitionResponseDTO> getWorks() {
		return works;
	}

	public void setWorks(List<WorkDefinitionResponseDTO> works) {
		this.works = works;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WorkDefinitionResponseDTO workDefinitionResponseDTO = (WorkDefinitionResponseDTO) o;
		return Objects.equals(this.author, workDefinitionResponseDTO.author)
				&& Objects.equals(this.id, workDefinitionResponseDTO.id)
				&& Objects.equals(this.name, workDefinitionResponseDTO.name)
				&& Objects.equals(this.outputs, workDefinitionResponseDTO.outputs)
				&& Objects.equals(this.parameters, workDefinitionResponseDTO.parameters)
				&& Objects.equals(this.processingType, workDefinitionResponseDTO.processingType)
				&& Objects.equals(this.workType, workDefinitionResponseDTO.workType)
				&& Objects.equals(this.works, workDefinitionResponseDTO.works);
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, id, name, outputs, parameters, processingType, workType, works);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WorkDefinitionResponseDTO {\n");
		sb.append("    author: ").append(toIndentedString(author)).append("\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
		sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
		sb.append("    processingType: ").append(toIndentedString(processingType)).append("\n");
		sb.append("    workType: ").append(toIndentedString(workType)).append("\n");
		sb.append("    works: ").append(toIndentedString(works)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces (except the
	 * first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

	public static HashSet<String> openapiFields;

	public static HashSet<String> openapiRequiredFields;

	static {
		// a set of all properties/fields (JSON key names)
		openapiFields = new HashSet<String>();
		openapiFields.add("author");
		openapiFields.add("id");
		openapiFields.add("name");
		openapiFields.add("outputs");
		openapiFields.add("parameters");
		openapiFields.add("processingType");
		openapiFields.add("workType");
		openapiFields.add("works");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to
	 * WorkDefinitionResponseDTO
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!WorkDefinitionResponseDTO.openapiRequiredFields.isEmpty()) { // has
																				// required
																				// fields
																				// but
																				// JSON
																				// object
																				// is null
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in WorkDefinitionResponseDTO is not found in the empty JSON string",
						WorkDefinitionResponseDTO.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!WorkDefinitionResponseDTO.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `WorkDefinitionResponseDTO` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if ((jsonObj.get("author") != null && !jsonObj.get("author").isJsonNull())
				&& !jsonObj.get("author").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `author` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("author").toString()));
		}
		if ((jsonObj.get("id") != null && !jsonObj.get("id").isJsonNull()) && !jsonObj.get("id").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `id` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("id").toString()));
		}
		if ((jsonObj.get("name") != null && !jsonObj.get("name").isJsonNull())
				&& !jsonObj.get("name").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `name` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("name").toString()));
		}
		// ensure the optional json data is an array if present
		if (jsonObj.get("outputs") != null && !jsonObj.get("outputs").isJsonArray()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `outputs` to be an array in the JSON string but got `%s`",
							jsonObj.get("outputs").toString()));
		}
		if ((jsonObj.get("processingType") != null && !jsonObj.get("processingType").isJsonNull())
				&& !jsonObj.get("processingType").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `processingType` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("processingType").toString()));
		}
		if ((jsonObj.get("workType") != null && !jsonObj.get("workType").isJsonNull())
				&& !jsonObj.get("workType").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `workType` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("workType").toString()));
		}
		if (jsonObj.get("works") != null && !jsonObj.get("works").isJsonNull()) {
			JsonArray jsonArrayworks = jsonObj.getAsJsonArray("works");
			if (jsonArrayworks != null) {
				// ensure the json data is an array
				if (!jsonObj.get("works").isJsonArray()) {
					throw new IllegalArgumentException(
							String.format("Expected the field `works` to be an array in the JSON string but got `%s`",
									jsonObj.get("works").toString()));
				}

				// validate the optional field `works` (array)
				for (int i = 0; i < jsonArrayworks.size(); i++) {
					WorkDefinitionResponseDTO.validateJsonObject(jsonArrayworks.get(i).getAsJsonObject());
				}
				;
			}
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!WorkDefinitionResponseDTO.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'WorkDefinitionResponseDTO' and
								// its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<WorkDefinitionResponseDTO> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(WorkDefinitionResponseDTO.class));

			return (TypeAdapter<T>) new TypeAdapter<WorkDefinitionResponseDTO>() {
				@Override
				public void write(JsonWriter out, WorkDefinitionResponseDTO value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public WorkDefinitionResponseDTO read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of WorkDefinitionResponseDTO given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of WorkDefinitionResponseDTO
	 * @throws IOException if the JSON string is invalid with respect to
	 * WorkDefinitionResponseDTO
	 */
	public static WorkDefinitionResponseDTO fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, WorkDefinitionResponseDTO.class);
	}

	/**
	 * Convert an instance of WorkDefinitionResponseDTO to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
