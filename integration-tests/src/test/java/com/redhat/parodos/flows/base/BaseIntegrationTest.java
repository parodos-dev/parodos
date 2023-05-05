package com.redhat.parodos.flows.base;

import com.redhat.parodos.sdk.invoker.ApiClient;
import com.redhat.parodos.sdk.invoker.ApiException;
import org.junit.Before;

import static com.redhat.parodos.sdkutils.SdkUtils.getParodosAPiClient;

/**
 * @author Gloria Ciavarrini (Github: gciavarrini)
 */
public class BaseIntegrationTest {

	protected ApiClient apiClient;

	@Before
	public void setUp() throws ApiException {
		apiClient = getParodosAPiClient();
	}

}
