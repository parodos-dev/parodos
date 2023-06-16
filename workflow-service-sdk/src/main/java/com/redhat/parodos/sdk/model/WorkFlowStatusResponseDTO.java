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
 * WorkFlowStatusResponseDTO
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class WorkFlowStatusResponseDTO {

	public static final String SERIALIZED_NAME_MESSAGE = "message";

	@SerializedName(SERIALIZED_NAME_MESSAGE)
	private String message;

	public static final String SERIALIZED_NAME_ORIGINAL_EXECUTION_ID = "originalExecutionId";

	@SerializedName(SERIALIZED_NAME_ORIGINAL_EXECUTION_ID)
	private UUID originalExecutionId;

	public static final String SERIALIZED_NAME_RESTARTED_COUNT = "restartedCount";

	@SerializedName(SERIALIZED_NAME_RESTARTED_COUNT)
	private Integer restartedCount;

	/**
	 * Gets or Sets status
	 */
	@JsonAdapter(StatusEnum.Adapter.class)
	public enum StatusEnum {

		FAILED("FAILED"),

		COMPLETED("COMPLETED"),

		IN_PROGRESS("IN_PROGRESS"),

		REJECTED("REJECTED"),

		PENDING("PENDING");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		public static StatusEnum fromValue(String value) {
			for (StatusEnum b : StatusEnum.values()) {
				if (b.value.equals(value)) {
					return b;
				}
			}
			throw new IllegalArgumentException("Unexpected value '" + value + "'");
		}

		public static class Adapter extends TypeAdapter<StatusEnum> {

			@Override
			public void write(final JsonWriter jsonWriter, final StatusEnum enumeration) throws IOException {
				jsonWriter.value(enumeration.getValue());
			}

			@Override
			public StatusEnum read(final JsonReader jsonReader) throws IOException {
				String value = jsonReader.nextString();
				return StatusEnum.fromValue(value);
			}

		}

	}

	public static final String SERIALIZED_NAME_STATUS = "status";

	@SerializedName(SERIALIZED_NAME_STATUS)
	private StatusEnum status;

	public static final String SERIALIZED_NAME_WORK_FLOW_EXECUTION_ID = "workFlowExecutionId";

	@SerializedName(SERIALIZED_NAME_WORK_FLOW_EXECUTION_ID)
	private UUID workFlowExecutionId;

	public static final String SERIALIZED_NAME_WORK_FLOW_NAME = "workFlowName";

	@SerializedName(SERIALIZED_NAME_WORK_FLOW_NAME)
	private String workFlowName;

	public static final String SERIALIZED_NAME_WORKS = "works";

	@SerializedName(SERIALIZED_NAME_WORKS)
	private List<WorkStatusResponseDTO> works;

	public WorkFlowStatusResponseDTO() {
	}

	public WorkFlowStatusResponseDTO message(String message) {

		this.message = message;
		return this;
	}

	/**
	 * Get message
	 * @return message
	 **/
	@javax.annotation.Nullable
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public WorkFlowStatusResponseDTO originalExecutionId(UUID originalExecutionId) {

		this.originalExecutionId = originalExecutionId;
		return this;
	}

	/**
	 * Get originalExecutionId
	 * @return originalExecutionId
	 **/
	@javax.annotation.Nullable
	public UUID getOriginalExecutionId() {
		return originalExecutionId;
	}

	public void setOriginalExecutionId(UUID originalExecutionId) {
		this.originalExecutionId = originalExecutionId;
	}

	public WorkFlowStatusResponseDTO restartedCount(Integer restartedCount) {

		this.restartedCount = restartedCount;
		return this;
	}

	/**
	 * Get restartedCount
	 * @return restartedCount
	 **/
	@javax.annotation.Nullable
	public Integer getRestartedCount() {
		return restartedCount;
	}

	public void setRestartedCount(Integer restartedCount) {
		this.restartedCount = restartedCount;
	}

	public WorkFlowStatusResponseDTO status(StatusEnum status) {

		this.status = status;
		return this;
	}

	/**
	 * Get status
	 * @return status
	 **/
	@javax.annotation.Nullable
	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public WorkFlowStatusResponseDTO workFlowExecutionId(UUID workFlowExecutionId) {

		this.workFlowExecutionId = workFlowExecutionId;
		return this;
	}

	/**
	 * Get workFlowExecutionId
	 * @return workFlowExecutionId
	 **/
	@javax.annotation.Nullable
	public UUID getWorkFlowExecutionId() {
		return workFlowExecutionId;
	}

	public void setWorkFlowExecutionId(UUID workFlowExecutionId) {
		this.workFlowExecutionId = workFlowExecutionId;
	}

	public WorkFlowStatusResponseDTO workFlowName(String workFlowName) {

		this.workFlowName = workFlowName;
		return this;
	}

	/**
	 * Get workFlowName
	 * @return workFlowName
	 **/
	@javax.annotation.Nullable
	public String getWorkFlowName() {
		return workFlowName;
	}

	public void setWorkFlowName(String workFlowName) {
		this.workFlowName = workFlowName;
	}

	public WorkFlowStatusResponseDTO works(List<WorkStatusResponseDTO> works) {

		this.works = works;
		return this;
	}

	public WorkFlowStatusResponseDTO addWorksItem(WorkStatusResponseDTO worksItem) {
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
	public List<WorkStatusResponseDTO> getWorks() {
		return works;
	}

	public void setWorks(List<WorkStatusResponseDTO> works) {
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
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = (WorkFlowStatusResponseDTO) o;
		return Objects.equals(this.message, workFlowStatusResponseDTO.message)
				&& Objects.equals(this.originalExecutionId, workFlowStatusResponseDTO.originalExecutionId)
				&& Objects.equals(this.restartedCount, workFlowStatusResponseDTO.restartedCount)
				&& Objects.equals(this.status, workFlowStatusResponseDTO.status)
				&& Objects.equals(this.workFlowExecutionId, workFlowStatusResponseDTO.workFlowExecutionId)
				&& Objects.equals(this.workFlowName, workFlowStatusResponseDTO.workFlowName)
				&& Objects.equals(this.works, workFlowStatusResponseDTO.works);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, originalExecutionId, restartedCount, status, workFlowExecutionId, workFlowName,
				works);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WorkFlowStatusResponseDTO {\n");
		sb.append("    message: ").append(toIndentedString(message)).append("\n");
		sb.append("    originalExecutionId: ").append(toIndentedString(originalExecutionId)).append("\n");
		sb.append("    restartedCount: ").append(toIndentedString(restartedCount)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    workFlowExecutionId: ").append(toIndentedString(workFlowExecutionId)).append("\n");
		sb.append("    workFlowName: ").append(toIndentedString(workFlowName)).append("\n");
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
		openapiFields.add("message");
		openapiFields.add("originalExecutionId");
		openapiFields.add("restartedCount");
		openapiFields.add("status");
		openapiFields.add("workFlowExecutionId");
		openapiFields.add("workFlowName");
		openapiFields.add("works");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to
	 * WorkFlowStatusResponseDTO
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!WorkFlowStatusResponseDTO.openapiRequiredFields.isEmpty()) { // has
																				// required
																				// fields
																				// but
																				// JSON
																				// object
																				// is null
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in WorkFlowStatusResponseDTO is not found in the empty JSON string",
						WorkFlowStatusResponseDTO.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!WorkFlowStatusResponseDTO.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `WorkFlowStatusResponseDTO` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if ((jsonObj.get("message") != null && !jsonObj.get("message").isJsonNull())
				&& !jsonObj.get("message").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `message` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("message").toString()));
		}
		if ((jsonObj.get("originalExecutionId") != null && !jsonObj.get("originalExecutionId").isJsonNull())
				&& !jsonObj.get("originalExecutionId").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `originalExecutionId` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("originalExecutionId").toString()));
		}
		if ((jsonObj.get("status") != null && !jsonObj.get("status").isJsonNull())
				&& !jsonObj.get("status").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `status` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("status").toString()));
		}
		if ((jsonObj.get("workFlowExecutionId") != null && !jsonObj.get("workFlowExecutionId").isJsonNull())
				&& !jsonObj.get("workFlowExecutionId").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `workFlowExecutionId` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("workFlowExecutionId").toString()));
		}
		if ((jsonObj.get("workFlowName") != null && !jsonObj.get("workFlowName").isJsonNull())
				&& !jsonObj.get("workFlowName").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `workFlowName` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("workFlowName").toString()));
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
					WorkStatusResponseDTO.validateJsonObject(jsonArrayworks.get(i).getAsJsonObject());
				}
				;
			}
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!WorkFlowStatusResponseDTO.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'WorkFlowStatusResponseDTO' and
								// its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<WorkFlowStatusResponseDTO> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(WorkFlowStatusResponseDTO.class));

			return (TypeAdapter<T>) new TypeAdapter<WorkFlowStatusResponseDTO>() {
				@Override
				public void write(JsonWriter out, WorkFlowStatusResponseDTO value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public WorkFlowStatusResponseDTO read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of WorkFlowStatusResponseDTO given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of WorkFlowStatusResponseDTO
	 * @throws IOException if the JSON string is invalid with respect to
	 * WorkFlowStatusResponseDTO
	 */
	public static WorkFlowStatusResponseDTO fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, WorkFlowStatusResponseDTO.class);
	}

	/**
	 * Convert an instance of WorkFlowStatusResponseDTO to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
