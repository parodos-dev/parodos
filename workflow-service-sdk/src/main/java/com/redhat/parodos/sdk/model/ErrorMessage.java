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
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.parodos.sdk.invoker.JSON;

/**
 * ErrorMessage
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class ErrorMessage {

	public static final String SERIALIZED_NAME_DATE = "date";

	@SerializedName(SERIALIZED_NAME_DATE)
	private Date date;

	public static final String SERIALIZED_NAME_DESCRIPTION = "description";

	@SerializedName(SERIALIZED_NAME_DESCRIPTION)
	private String description;

	public static final String SERIALIZED_NAME_MESSAGE = "message";

	@SerializedName(SERIALIZED_NAME_MESSAGE)
	private String message;

	public static final String SERIALIZED_NAME_STATUS = "status";

	@SerializedName(SERIALIZED_NAME_STATUS)
	private Integer status;

	public ErrorMessage() {
	}

	public ErrorMessage date(Date date) {

		this.date = date;
		return this;
	}

	/**
	 * Get date
	 * @return date
	 **/
	@javax.annotation.Nullable

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public ErrorMessage description(String description) {

		this.description = description;
		return this;
	}

	/**
	 * Get description
	 * @return description
	 **/
	@javax.annotation.Nullable

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ErrorMessage message(String message) {

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

	public ErrorMessage status(Integer status) {

		this.status = status;
		return this;
	}

	/**
	 * Get status
	 * @return status
	 **/
	@javax.annotation.Nullable

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ErrorMessage errorMessage = (ErrorMessage) o;
		return Objects.equals(this.date, errorMessage.date)
				&& Objects.equals(this.description, errorMessage.description)
				&& Objects.equals(this.message, errorMessage.message)
				&& Objects.equals(this.status, errorMessage.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(date, description, message, status);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ErrorMessage {\n");
		sb.append("    date: ").append(toIndentedString(date)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    message: ").append(toIndentedString(message)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
		openapiFields.add("date");
		openapiFields.add("description");
		openapiFields.add("message");
		openapiFields.add("status");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to ErrorMessage
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!ErrorMessage.openapiRequiredFields.isEmpty()) { // has required fields
																	// but JSON object is
																	// null
				throw new IllegalArgumentException(
						String.format("The required field(s) %s in ErrorMessage is not found in the empty JSON string",
								ErrorMessage.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!ErrorMessage.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `ErrorMessage` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if ((jsonObj.get("description") != null && !jsonObj.get("description").isJsonNull())
				&& !jsonObj.get("description").isJsonPrimitive()) {
			throw new IllegalArgumentException(String.format(
					"Expected the field `description` to be a primitive type in the JSON string but got `%s`",
					jsonObj.get("description").toString()));
		}
		if ((jsonObj.get("message") != null && !jsonObj.get("message").isJsonNull())
				&& !jsonObj.get("message").isJsonPrimitive()) {
			throw new IllegalArgumentException(
					String.format("Expected the field `message` to be a primitive type in the JSON string but got `%s`",
							jsonObj.get("message").toString()));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!ErrorMessage.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'ErrorMessage' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<ErrorMessage> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(ErrorMessage.class));

			return (TypeAdapter<T>) new TypeAdapter<ErrorMessage>() {
				@Override
				public void write(JsonWriter out, ErrorMessage value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public ErrorMessage read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of ErrorMessage given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of ErrorMessage
	 * @throws IOException if the JSON string is invalid with respect to ErrorMessage
	 */
	public static ErrorMessage fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, ErrorMessage.class);
	}

	/**
	 * Convert an instance of ErrorMessage to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
