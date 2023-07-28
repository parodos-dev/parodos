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
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.redhat.parodos.notification.sdk.api.JSON;

/**
 * PageNotificationRecordResponseDTO
 */
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class PageNotificationRecordResponseDTO {

	public static final String SERIALIZED_NAME_CONTENT = "content";

	@SerializedName(SERIALIZED_NAME_CONTENT)
	private List<NotificationRecordResponseDTO> content;

	public static final String SERIALIZED_NAME_EMPTY = "empty";

	@SerializedName(SERIALIZED_NAME_EMPTY)
	private Boolean empty;

	public static final String SERIALIZED_NAME_FIRST = "first";

	@SerializedName(SERIALIZED_NAME_FIRST)
	private Boolean first;

	public static final String SERIALIZED_NAME_LAST = "last";

	@SerializedName(SERIALIZED_NAME_LAST)
	private Boolean last;

	public static final String SERIALIZED_NAME_NUMBER = "number";

	@SerializedName(SERIALIZED_NAME_NUMBER)
	private Integer number;

	public static final String SERIALIZED_NAME_NUMBER_OF_ELEMENTS = "numberOfElements";

	@SerializedName(SERIALIZED_NAME_NUMBER_OF_ELEMENTS)
	private Integer numberOfElements;

	public static final String SERIALIZED_NAME_PAGEABLE = "pageable";

	@SerializedName(SERIALIZED_NAME_PAGEABLE)
	private PageableObject pageable;

	public static final String SERIALIZED_NAME_SIZE = "size";

	@SerializedName(SERIALIZED_NAME_SIZE)
	private Integer size;

	public static final String SERIALIZED_NAME_SORT = "sort";

	@SerializedName(SERIALIZED_NAME_SORT)
	private SortObject sort;

	public static final String SERIALIZED_NAME_TOTAL_ELEMENTS = "totalElements";

	@SerializedName(SERIALIZED_NAME_TOTAL_ELEMENTS)
	private Long totalElements;

	public static final String SERIALIZED_NAME_TOTAL_PAGES = "totalPages";

	@SerializedName(SERIALIZED_NAME_TOTAL_PAGES)
	private Integer totalPages;

	public PageNotificationRecordResponseDTO() {
	}

	public PageNotificationRecordResponseDTO content(List<NotificationRecordResponseDTO> content) {

		this.content = content;
		return this;
	}

	public PageNotificationRecordResponseDTO addContentItem(NotificationRecordResponseDTO contentItem) {
		if (this.content == null) {
			this.content = new ArrayList<>();
		}
		this.content.add(contentItem);
		return this;
	}

	/**
	 * Get content
	 * @return content
	 **/
	@jakarta.annotation.Nullable
	public List<NotificationRecordResponseDTO> getContent() {
		return content;
	}

	public void setContent(List<NotificationRecordResponseDTO> content) {
		this.content = content;
	}

	public PageNotificationRecordResponseDTO empty(Boolean empty) {

		this.empty = empty;
		return this;
	}

	/**
	 * Get empty
	 * @return empty
	 **/
	@jakarta.annotation.Nullable
	public Boolean getEmpty() {
		return empty;
	}

	public void setEmpty(Boolean empty) {
		this.empty = empty;
	}

	public PageNotificationRecordResponseDTO first(Boolean first) {

		this.first = first;
		return this;
	}

	/**
	 * Get first
	 * @return first
	 **/
	@jakarta.annotation.Nullable
	public Boolean getFirst() {
		return first;
	}

	public void setFirst(Boolean first) {
		this.first = first;
	}

	public PageNotificationRecordResponseDTO last(Boolean last) {

		this.last = last;
		return this;
	}

	/**
	 * Get last
	 * @return last
	 **/
	@jakarta.annotation.Nullable
	public Boolean getLast() {
		return last;
	}

	public void setLast(Boolean last) {
		this.last = last;
	}

	public PageNotificationRecordResponseDTO number(Integer number) {

		this.number = number;
		return this;
	}

	/**
	 * Get number
	 * @return number
	 **/
	@jakarta.annotation.Nullable
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public PageNotificationRecordResponseDTO numberOfElements(Integer numberOfElements) {

		this.numberOfElements = numberOfElements;
		return this;
	}

	/**
	 * Get numberOfElements
	 * @return numberOfElements
	 **/
	@jakarta.annotation.Nullable
	public Integer getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(Integer numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public PageNotificationRecordResponseDTO pageable(PageableObject pageable) {

		this.pageable = pageable;
		return this;
	}

	/**
	 * Get pageable
	 * @return pageable
	 **/
	@jakarta.annotation.Nullable
	public PageableObject getPageable() {
		return pageable;
	}

	public void setPageable(PageableObject pageable) {
		this.pageable = pageable;
	}

	public PageNotificationRecordResponseDTO size(Integer size) {

		this.size = size;
		return this;
	}

	/**
	 * Get size
	 * @return size
	 **/
	@jakarta.annotation.Nullable
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public PageNotificationRecordResponseDTO sort(SortObject sort) {

		this.sort = sort;
		return this;
	}

	/**
	 * Get sort
	 * @return sort
	 **/
	@jakarta.annotation.Nullable
	public SortObject getSort() {
		return sort;
	}

	public void setSort(SortObject sort) {
		this.sort = sort;
	}

	public PageNotificationRecordResponseDTO totalElements(Long totalElements) {

		this.totalElements = totalElements;
		return this;
	}

	/**
	 * Get totalElements
	 * @return totalElements
	 **/
	@jakarta.annotation.Nullable
	public Long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(Long totalElements) {
		this.totalElements = totalElements;
	}

	public PageNotificationRecordResponseDTO totalPages(Integer totalPages) {

		this.totalPages = totalPages;
		return this;
	}

	/**
	 * Get totalPages
	 * @return totalPages
	 **/
	@jakarta.annotation.Nullable
	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PageNotificationRecordResponseDTO pageNotificationRecordResponseDTO = (PageNotificationRecordResponseDTO) o;
		return Objects.equals(this.content, pageNotificationRecordResponseDTO.content)
				&& Objects.equals(this.empty, pageNotificationRecordResponseDTO.empty)
				&& Objects.equals(this.first, pageNotificationRecordResponseDTO.first)
				&& Objects.equals(this.last, pageNotificationRecordResponseDTO.last)
				&& Objects.equals(this.number, pageNotificationRecordResponseDTO.number)
				&& Objects.equals(this.numberOfElements, pageNotificationRecordResponseDTO.numberOfElements)
				&& Objects.equals(this.pageable, pageNotificationRecordResponseDTO.pageable)
				&& Objects.equals(this.size, pageNotificationRecordResponseDTO.size)
				&& Objects.equals(this.sort, pageNotificationRecordResponseDTO.sort)
				&& Objects.equals(this.totalElements, pageNotificationRecordResponseDTO.totalElements)
				&& Objects.equals(this.totalPages, pageNotificationRecordResponseDTO.totalPages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, empty, first, last, number, numberOfElements, pageable, size, sort, totalElements,
				totalPages);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PageNotificationRecordResponseDTO {\n");
		sb.append("    content: ").append(toIndentedString(content)).append("\n");
		sb.append("    empty: ").append(toIndentedString(empty)).append("\n");
		sb.append("    first: ").append(toIndentedString(first)).append("\n");
		sb.append("    last: ").append(toIndentedString(last)).append("\n");
		sb.append("    number: ").append(toIndentedString(number)).append("\n");
		sb.append("    numberOfElements: ").append(toIndentedString(numberOfElements)).append("\n");
		sb.append("    pageable: ").append(toIndentedString(pageable)).append("\n");
		sb.append("    size: ").append(toIndentedString(size)).append("\n");
		sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
		sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
		sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
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
		openapiFields.add("content");
		openapiFields.add("empty");
		openapiFields.add("first");
		openapiFields.add("last");
		openapiFields.add("number");
		openapiFields.add("numberOfElements");
		openapiFields.add("pageable");
		openapiFields.add("size");
		openapiFields.add("sort");
		openapiFields.add("totalElements");
		openapiFields.add("totalPages");

		// a set of required properties/fields (JSON key names)
		openapiRequiredFields = new HashSet<String>();
	}

	/**
	 * Validates the JSON Object and throws an exception if issues found
	 * @param jsonObj JSON Object
	 * @throws IOException if the JSON Object is invalid with respect to
	 * PageNotificationRecordResponseDTO
	 */
	public static void validateJsonObject(JsonObject jsonObj) throws IOException {
		if (jsonObj == null) {
			if (!PageNotificationRecordResponseDTO.openapiRequiredFields.isEmpty()) { // has
																						// required
																						// fields
																						// but
																						// JSON
																						// object
																						// is
																						// null
				throw new IllegalArgumentException(String.format(
						"The required field(s) %s in PageNotificationRecordResponseDTO is not found in the empty JSON string",
						PageNotificationRecordResponseDTO.openapiRequiredFields.toString()));
			}
		}

		Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
		// check to see if the JSON string contains additional fields
		for (Entry<String, JsonElement> entry : entries) {
			if (!PageNotificationRecordResponseDTO.openapiFields.contains(entry.getKey())) {
				throw new IllegalArgumentException(String.format(
						"The field `%s` in the JSON string is not defined in the `PageNotificationRecordResponseDTO` properties. JSON: %s",
						entry.getKey(), jsonObj.toString()));
			}
		}
		if (jsonObj.get("content") != null && !jsonObj.get("content").isJsonNull()) {
			JsonArray jsonArraycontent = jsonObj.getAsJsonArray("content");
			if (jsonArraycontent != null) {
				// ensure the json data is an array
				if (!jsonObj.get("content").isJsonArray()) {
					throw new IllegalArgumentException(
							String.format("Expected the field `content` to be an array in the JSON string but got `%s`",
									jsonObj.get("content").toString()));
				}

				// validate the optional field `content` (array)
				for (int i = 0; i < jsonArraycontent.size(); i++) {
					NotificationRecordResponseDTO.validateJsonObject(jsonArraycontent.get(i).getAsJsonObject());
				}
				;
			}
		}
		// validate the optional field `pageable`
		if (jsonObj.get("pageable") != null && !jsonObj.get("pageable").isJsonNull()) {
			PageableObject.validateJsonObject(jsonObj.getAsJsonObject("pageable"));
		}
		// validate the optional field `sort`
		if (jsonObj.get("sort") != null && !jsonObj.get("sort").isJsonNull()) {
			SortObject.validateJsonObject(jsonObj.getAsJsonObject("sort"));
		}
	}

	public static class CustomTypeAdapterFactory implements TypeAdapterFactory {

		@SuppressWarnings("unchecked")
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!PageNotificationRecordResponseDTO.class.isAssignableFrom(type.getRawType())) {
				return null; // this class only serializes
								// 'PageNotificationRecordResponseDTO' and its subtypes
			}
			final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
			final TypeAdapter<PageNotificationRecordResponseDTO> thisAdapter = gson.getDelegateAdapter(this,
					TypeToken.get(PageNotificationRecordResponseDTO.class));

			return (TypeAdapter<T>) new TypeAdapter<PageNotificationRecordResponseDTO>() {
				@Override
				public void write(JsonWriter out, PageNotificationRecordResponseDTO value) throws IOException {
					JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
					elementAdapter.write(out, obj);
				}

				@Override
				public PageNotificationRecordResponseDTO read(JsonReader in) throws IOException {
					JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
					validateJsonObject(jsonObj);
					return thisAdapter.fromJsonTree(jsonObj);
				}

			}.nullSafe();
		}

	}

	/**
	 * Create an instance of PageNotificationRecordResponseDTO given an JSON string
	 * @param jsonString JSON string
	 * @return An instance of PageNotificationRecordResponseDTO
	 * @throws IOException if the JSON string is invalid with respect to
	 * PageNotificationRecordResponseDTO
	 */
	public static PageNotificationRecordResponseDTO fromJson(String jsonString) throws IOException {
		return JSON.getGson().fromJson(jsonString, PageNotificationRecordResponseDTO.class);
	}

	/**
	 * Convert an instance of PageNotificationRecordResponseDTO to an JSON string
	 * @return JSON string
	 */
	public String toJson() {
		return JSON.getGson().toJson(this);
	}

}
