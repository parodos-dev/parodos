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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * WorkStatusResponseDTO
 */
@lombok.Data
@lombok.AllArgsConstructor
@lombok.Builder
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class WorkStatusResponseDTO {

	public static final String SERIALIZED_NAME_NAME = "name";

	@SerializedName(SERIALIZED_NAME_NAME)
	private String name;

	/**
	 * Gets or Sets status
	 */
	@JsonAdapter(StatusEnum.Adapter.class)
	public enum StatusEnum {

		FAILED("FAILED"),

		COMPLETED("COMPLETED"),

		PENDING("PENDING"),

		IN_PROGRESS("IN_PROGRESS"),

		REJECTED("REJECTED");

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

	public WorkStatusResponseDTO() {
	}

	public WorkStatusResponseDTO name(String name) {

		this.name = name;
		return this;
	}

	/**
	 * Get name
	 * @return name
	 **/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public WorkStatusResponseDTO status(StatusEnum status) {

		this.status = status;
		return this;
	}

	/**
	 * Get status
	 * @return status
	 **/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public WorkStatusResponseDTO type(TypeEnum type) {

		this.type = type;
		return this;
	}

	/**
	 * Get type
	 * @return type
	 **/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public TypeEnum getType() {
		return type;
	}

	public void setType(TypeEnum type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WorkStatusResponseDTO workStatusResponseDTO = (WorkStatusResponseDTO) o;
		return Objects.equals(this.name, workStatusResponseDTO.name)
				&& Objects.equals(this.status, workStatusResponseDTO.status)
				&& Objects.equals(this.type, workStatusResponseDTO.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, status, type);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WorkStatusResponseDTO {\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

}
