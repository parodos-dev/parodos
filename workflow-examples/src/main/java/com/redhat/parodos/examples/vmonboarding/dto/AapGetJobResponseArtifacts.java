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
public class AapGetJobResponseArtifacts {

	@JsonProperty("azure_vm_public_ip")
	private String azureVmPublicIp;

}
