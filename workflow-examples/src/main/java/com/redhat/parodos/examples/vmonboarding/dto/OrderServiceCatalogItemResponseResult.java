package com.redhat.parodos.examples.vmonboarding.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderServiceCatalogItemResponseResult {

	@JsonProperty("sys_id")
	private String sysId;

	private String number;

}
