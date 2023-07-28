package com.redhat.parodos.infrastructure;

import java.util.UUID;

import com.redhat.parodos.sdk.invoker.ApiException;
import com.redhat.parodos.sdk.model.AccessRequestDTO;
import com.redhat.parodos.sdk.model.AccessResponseDTO;
import com.redhat.parodos.sdk.model.AccessStatusResponseDTO;

public interface ProjectRequester {

	String getBasePath();

	AccessResponseDTO createAccess(UUID id, AccessRequestDTO accessRequestDTO) throws ApiException;

	AccessStatusResponseDTO getAccessStatus(UUID id) throws ApiException;

}
