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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

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
 * WorkRequestDTO
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class WorkRequestDTO {

	public static final String SERIALIZED_NAME_ARGUMENTS = "arguments";

	@SerializedName(SERIALIZED_NAME_ARGUMENTS)
	private List<ArgumentRequestDTO> arguments = new ArrayList<>();

	/**
	 * Gets or Sets type
	 */
	@JsonAdapter(TypeEnum.Adapter.class)
	public enum TypeEnum {

		TASK("TASK"),

		WORKFLOW("WORKFLOW");

		private String value;

		TypeEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static TypeEnum fromValue(String value) {
			for (TypeEnum b : TypeEnum.values()) {
				if (b.value.equals(value)) {
					return b;
				}
			}
			throw new IllegalArgumentException("Unexpected value '" + value + "'");
		}

		public static class Adapter extends TypeAdapter<TypeEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final TypeEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public TypeEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return TypeEnum.fromValue(value);
			}

		}

	}

	public static final String SERIALIZED_NAME_TYPE = "type";

	@SerializedName(SERIALIZED_NAME_TYPE)
	private TypeEnum type;

	public static final String SERIALIZED_NAME_WORK_NAME = "workName";

	@SerializedName(SERIALIZED_NAME_WORK_NAME)
	private String workName;

	public WorkRequestDTO() {
	}

	public WorkRequestDTO arguments(List<ArgumentRequestDTO> arguments) {

		this.arguments = arguments;
		return this;
	}

	public WorkRequestDTO addArgumentsItem(ArgumentRequestDTO argumentsItem) {
		if (this.arguments == null) {
			this.arguments = new ArrayList<>();
		}
		this.arguments.add(argumentsItem);
		return this;
	}

	/**
	 * Get arguments
	 * @return arguments
	 **/
	@javax.annotation.Nullable

	public List<ArgumentRequestDTO> getArguments() {
		return arguments;
	}

	public void setArguments(List<ArgumentRequestDTO> arguments) {
		this.arguments = arguments;
	}

	public WorkRequestDTO type(TypeEnum type) {

		this.type = type;
		return this;
	}

	/**
	 * Get type
	 * @return type
	 **/
	@javax.annotation.Nullable

	public TypeEnum getType() {
		return type;
	}

	public void setType(TypeEnum type) {
		this.type = type;
	}

	public WorkRequestDTO workName(String workName) {

		this.workName = workName;
		return this;
	}

	/**
	 * Get workName
	 * @return workName
	 **/
	@javax.annotation.Nullable

	public String getWorkName() {
		return workName;
	}

	public void setWorkName(String workName) {
		this.workName = workName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WorkRequestDTO workRequestDTO = (WorkRequestDTO) o;
		return Objects.equals(this.arguments, workRequestDTO.arguments)
				&& Objects.equals(this.type, workRequestDTO.type)
				&& Objects.equals(this.workName, workRequestDTO.workName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(arguments, type, workName);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WorkRequestDTO {\n");
		sb.append("    arguments: ").append(toIndentedString(arguments)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    workName: ").append(toIndentedString(workName)).append("\n");
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
		openapiFields.add("arguments");
		openapiFields.add("type");
		openapiFields.add("workName");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to WorkRequestDTO
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!WorkRequestDTO.openapiRequiredFields.isEmpty()) { // has required fields
																	// but JSON object is
																	// null
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in WorkRequestDTO is not found in the empty JSON string",
						WorkRequestDTO.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!WorkRequestDTO.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `WorkRequestDTO` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if (jsonObj.get("arguments") != null && !jsonObj.get("arguments").isJsonNull()) {
			JsonArray jsonArrayarguments = jsonObj.getAsJsonArray("arguments");
			if (jsonArrayarguments != null) {
				// ensure the json data is an array
				if (!jsonObj.get("arguments").isJsonArray()) {
					throw new IllegalArgumentException(String.format(
							"Expected the field `arguments` to be an array in the JSON string but got `%s`",
							jsonObj.get("arguments").toString()));
				}

				// validate the optional field `arguments` (array)
				for (int i = 0; i < jsonArrayarguments.size(); i++) {
					ArgumentRequestDTO.validateJsonObject(jsonArrayarguments.get(i).getAsJsonObject());
				}
				;
			}
		}
		if ((jsonObj.get("type") != null && !jsonObj.get("type").isJsonNull())
				&& !jsonObj.get("type").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `type` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("type").toString()));
		}
		if ((jsonObj.get("workName") != null && !jsonObj.get("workName").isJsonNull())
				&& !jsonObj.get("workName").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `workName` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("workName").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!WorkRequestDTO.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'WorkRequestDTO' and its
								// subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<WorkRequestDTO> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(WorkRequestDTO.class));

			return (TypeAdapter<T>) new TypeAdapter<WorkRequestDTO>() {
				@Override
				public void write(JsonWriter out, WorkRequestDTO value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public WorkRequestDTO read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of WorkRequestDTO given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of WorkRequestDTO
	 * @throws IOException if the JSON string is invalid with respect to WorkRequestDTO
	 */
	public static WorkRequestDTO fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, WorkRequestDTO.class);
	}

	/**
	 * Convert an instance of WorkRequestDTO to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
