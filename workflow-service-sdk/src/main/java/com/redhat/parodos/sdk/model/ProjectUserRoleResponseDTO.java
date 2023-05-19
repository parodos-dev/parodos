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

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.parodos.sdk.model.UserRoleResponseDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.redhat.parodos.sdk.invoker.JSON;

/**
 * ProjectUserRoleResponseDTO
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class ProjectUserRoleResponseDTO {

	public static final String SERIALIZED_NAME_ID = "id";

	@SerializedName(SERIALIZED_NAME_ID)
	private UUID id;

	public static final String SERIALIZED_NAME_PROJECT_NAME = "projectName";

	@SerializedName(SERIALIZED_NAME_PROJECT_NAME)
	private String projectName;

	public static final String SERIALIZED_NAME_USER_RESPONSE_D_T_O_LIST = "userResponseDTOList";

	@SerializedName(SERIALIZED_NAME_USER_RESPONSE_D_T_O_LIST)
	private List<UserRoleResponseDTO> userResponseDTOList = new ArrayList<>();

	public ProjectUserRoleResponseDTO() {
	}

	public ProjectUserRoleResponseDTO id(UUID id) {

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

	public ProjectUserRoleResponseDTO projectName(String projectName) {

		this.projectName = projectName;
		return this;
	}

	/**
	 * Get projectName
	 * @return projectName
	 **/
	@javax.annotation.Nullable

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public ProjectUserRoleResponseDTO userResponseDTOList(List<UserRoleResponseDTO> userResponseDTOList) {

		this.userResponseDTOList = userResponseDTOList;
		return this;
	}

	public ProjectUserRoleResponseDTO addUserResponseDTOListItem(UserRoleResponseDTO userResponseDTOListItem) {
		if (this.userResponseDTOList == null) {
			this.userResponseDTOList = new ArrayList<>();
		}
		this.userResponseDTOList.add(userResponseDTOListItem);
		return this;
	}

	/**
	 * Get userResponseDTOList
	 * @return userResponseDTOList
	 **/
	@javax.annotation.Nullable

	public List<UserRoleResponseDTO> getUserResponseDTOList() {
		return userResponseDTOList;
	}

	public void setUserResponseDTOList(List<UserRoleResponseDTO> userResponseDTOList) {
		this.userResponseDTOList = userResponseDTOList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectUserRoleResponseDTO projectUserRoleResponseDTO = (ProjectUserRoleResponseDTO) o;
		return Objects.equals(this.id, projectUserRoleResponseDTO.id)
				&& Objects.equals(this.projectName, projectUserRoleResponseDTO.projectName)
				&& Objects.equals(this.userResponseDTOList, projectUserRoleResponseDTO.userResponseDTOList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectName, userResponseDTOList);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ProjectUserRoleResponseDTO {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    projectName: ").append(toIndentedString(projectName)).append("\n");
		sb.append("    userResponseDTOList: ").append(toIndentedString(userResponseDTOList)).append("\n");
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
		openapiFields.add("id");
		openapiFields.add("projectName");
		openapiFields.add("userResponseDTOList");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to
	 * ProjectUserRoleResponseDTO
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!ProjectUserRoleResponseDTO.openapiRequiredFields.isEmpty()) { // has
																				// required
																				// fields
																				// but
																				// JSON
																				// object
																				// is null
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in ProjectUserRoleResponseDTO is not found in the empty JSON string",
						ProjectUserRoleResponseDTO.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!ProjectUserRoleResponseDTO.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `ProjectUserRoleResponseDTO` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if ((jsonObj.get("id") != null && !jsonObj.get("id").isJsonNull()) && !jsonObj.get("id").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `id` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("id").toString()));
		}
		if ((jsonObj.get("projectName") != null && !jsonObj.get("projectName").isJsonNull())
				&& !jsonObj.get("projectName").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `projectName` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("projectName").toString()));
		}
		if (jsonObj.get("userResponseDTOList") != null && !jsonObj.get("userResponseDTOList").isJsonNull()) {
			JsonArray jsonArrayuserResponseDTOList = jsonObj.getAsJsonArray("userResponseDTOList");
			if (jsonArrayuserResponseDTOList != null) {
				// ensure the json data is an array
				if (!jsonObj.get("userResponseDTOList").isJsonArray()) {
					throw new IllegalArgumentException(String.format(
							"Expected the field `userResponseDTOList` to be an array in the JSON string but got `%s`",
							jsonObj.get("userResponseDTOList").toString()));
				}

				// validate the optional field `userResponseDTOList` (array)
				for (int i = 0; i < jsonArrayuserResponseDTOList.size(); i++) {
					UserRoleResponseDTO.validateJsonObject(jsonArrayuserResponseDTOList.get(i).getAsJsonObject());
				}
				;
			}
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!ProjectUserRoleResponseDTO.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'ProjectUserRoleResponseDTO'
								// and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<ProjectUserRoleResponseDTO> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(ProjectUserRoleResponseDTO.class));

			return (TypeAdapter<T>) new TypeAdapter<ProjectUserRoleResponseDTO>() {
				@Override
				public void write(JsonWriter out, ProjectUserRoleResponseDTO value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public ProjectUserRoleResponseDTO read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of ProjectUserRoleResponseDTO given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of ProjectUserRoleResponseDTO
	 * @throws IOException if the JSON string is invalid with respect to
	 * ProjectUserRoleResponseDTO
	 */
	public static ProjectUserRoleResponseDTO fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, ProjectUserRoleResponseDTO.class);
	}

	/**
	 * Convert an instance of ProjectUserRoleResponseDTO to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
