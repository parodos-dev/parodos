/*
 * Parodos Notification Service API
 * This is the API documentation for the Parodos Notification Service. It provides operations to send out and check notification. The endpoints are secured with oAuth2/OpenID and cannot be accessed without a valid token.
 *
 * The version of the OpenAPI document: v1.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.redhat.parodos.notification.sdk.model;

import java.io.IOException;
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
import com.redhat.parodos.notification.sdk.api.JSON;

/**
 * PageableObject
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class PageableObject {

	public static final String SERIALIZED_NAME_OFFSET = "offset";

	@SerializedName(SERIALIZED_NAME_OFFSET)
	private Long offset;

	public static final String SERIALIZED_NAME_PAGE_NUMBER = "pageNumber";

	@SerializedName(SERIALIZED_NAME_PAGE_NUMBER)
	private Integer pageNumber;

	public static final String SERIALIZED_NAME_PAGE_SIZE = "pageSize";

	@SerializedName(SERIALIZED_NAME_PAGE_SIZE)
	private Integer pageSize;

	public static final String SERIALIZED_NAME_PAGED = "paged";

	@SerializedName(SERIALIZED_NAME_PAGED)
	private Boolean paged;

	public static final String SERIALIZED_NAME_SORT = "sort";

	@SerializedName(SERIALIZED_NAME_SORT)
	private Sort sort;

	public static final String SERIALIZED_NAME_UNPAGED = "unpaged";

	@SerializedName(SERIALIZED_NAME_UNPAGED)
	private Boolean unpaged;

	public PageableObject() {
	}

	public PageableObject offset(Long offset) {

		this.offset = offset;
		return this;
	}

	/**
	 * Get offset
	 * @return offset
	 **/
	@javax.annotation.Nullable

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public PageableObject pageNumber(Integer pageNumber) {

		this.pageNumber = pageNumber;
		return this;
	}

	/**
	 * Get pageNumber
	 * @return pageNumber
	 **/
	@javax.annotation.Nullable

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public PageableObject pageSize(Integer pageSize) {

		this.pageSize = pageSize;
		return this;
	}

	/**
	 * Get pageSize
	 * @return pageSize
	 **/
	@javax.annotation.Nullable

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public PageableObject paged(Boolean paged) {

		this.paged = paged;
		return this;
	}

	/**
	 * Get paged
	 * @return paged
	 **/
	@javax.annotation.Nullable

	public Boolean getPaged() {
		return paged;
	}

	public void setPaged(Boolean paged) {
		this.paged = paged;
	}

	public PageableObject sort(Sort sort) {

		this.sort = sort;
		return this;
	}

	/**
	 * Get sort
	 * @return sort
	 **/
	@javax.annotation.Nullable

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public PageableObject unpaged(Boolean unpaged) {

		this.unpaged = unpaged;
		return this;
	}

	/**
	 * Get unpaged
	 * @return unpaged
	 **/
	@javax.annotation.Nullable

	public Boolean getUnpaged() {
		return unpaged;
	}

	public void setUnpaged(Boolean unpaged) {
		this.unpaged = unpaged;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PageableObject pageableObject = (PageableObject) o;
		return Objects.equals(this.offset, pageableObject.offset)
				&& Objects.equals(this.pageNumber, pageableObject.pageNumber)
				&& Objects.equals(this.pageSize, pageableObject.pageSize)
				&& Objects.equals(this.paged, pageableObject.paged) && Objects.equals(this.sort, pageableObject.sort)
				&& Objects.equals(this.unpaged, pageableObject.unpaged);
	}

	@Override
	public int hashCode() {
		return Objects.hash(offset, pageNumber, pageSize, paged, sort, unpaged);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PageableObject {\n");
		sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
		sb.append("    pageNumber: ").append(toIndentedString(pageNumber)).append("\n");
		sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
		sb.append("    paged: ").append(toIndentedString(paged)).append("\n");
		sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
		sb.append("    unpaged: ").append(toIndentedString(unpaged)).append("\n");
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
		openapiFields.add("offset");
		openapiFields.add("pageNumber");
		openapiFields.add("pageSize");
		openapiFields.add("paged");
		openapiFields.add("sort");
		openapiFields.add("unpaged");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to PageableObject
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!PageableObject.openapiRequiredFields.isEmpty()) { // has required fields
																	// but JSON object is
																	// null
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in PageableObject is not found in the empty JSON string",
						PageableObject.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!PageableObject.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `PageableObject` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		// validate the optional field `sort`
		if (jsonObj.get("sort") != null && !jsonObj.get("sort").isJsonNull()) {
			Sort.validateJsonObject(jsonObj.getAsJsonObject("sort"));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!PageableObject.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes 'PageableObject' and its
								// subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<PageableObject> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(PageableObject.class));

			return (TypeAdapter<T>) new TypeAdapter<PageableObject>() {
				@Override
				public void write(JsonWriter out, PageableObject value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public PageableObject read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of PageableObject given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of PageableObject
	 * @throws IOException if the JSON string is invalid with respect to PageableObject
	 */
	public static PageableObject fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, PageableObject.class);
	}

	/**
	 * Convert an instance of PageableObject to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
