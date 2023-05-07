/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.notification.controller;

import javax.transaction.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.redhat.parodos.notification.dto.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.notification.enums.Operation;
import com.redhat.parodos.notification.enums.State;
import com.redhat.parodos.notification.jpa.NotificationsDataCreator;
import com.redhat.parodos.notification.jpa.repository.NotificationGroupRepository;
import com.redhat.parodos.notification.jpa.repository.NotificationUserRepository;
import com.redhat.parodos.notification.util.SecurityUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */

@Transactional
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationRecordControllerTests extends AbstractControllerTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private NotificationUserRepository notificationUserRepository;

	@Autowired
	private NotificationGroupRepository notificationGroupRepository;

	@MockBean
	private SecurityUtil securityUtil;

	private MockMvc mockMvc;

	@TestConfiguration
	private class TestConfig {

		@Bean
		SecurityUtil securityUtils() {
			return securityUtil;
		}

	}

	@BeforeEach
	public void setup() throws Exception {
		when(securityUtil.getUsername()).thenReturn(NotificationsDataCreator.USER_A_1);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}

	@Test
	void getNotificationsWithUsernameOnly() throws Exception {
		// Creates GROUP_A and GROUP_B with
		// users USER_A_1, USER_A2 in GROUP_A and USER_B_1,USER_B_2 in GROUP_B
		NotificationsDataCreator.createAndSaveTwoGroupsAndTwoUsersPerGroup(this.notificationGroupRepository,
				this.notificationUserRepository);

		NotificationMessageCreateRequestDTO createRequest = createRequestForUser1A();

		this.mockMvc.perform(postJson("/api/v1/messages", createRequest)).andDo(print())
				.andExpect(status().isCreated());
		// assertions for this are already in NotificationsMessageControllerTests

		// get the notifications for user_1_a
		this.mockMvc
				.perform(get("/api/v1/notifications").accept(MediaType.APPLICATION_JSON)
						.param("username", NotificationsDataCreator.USER_A_1).param("page", "0").param("size", "10"))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].subject", Matchers.is(NotificationsDataCreator.MESSAGE_SUBJECT)))
				.andReturn();

		MvcResult mvcResult = this.mockMvc
				.perform(get("/api/v1/notifications").accept(MediaType.APPLICATION_JSON)
						.param("username", NotificationsDataCreator.USER_A_1).param("status", State.UNREAD.toString())
						.param("page", "0").param("size", "10"))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].subject", Matchers.is(NotificationsDataCreator.MESSAGE_SUBJECT)))
				.andExpect(jsonPath("$.content[0].read", is(false))).andReturn();

		String id = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.content[0].id");

		this.mockMvc.perform(put("/api/v1/notifications/{id}", id).param("operation", Operation.READ.toString())
				.accept(MediaType.APPLICATION_JSON)).andDo(print());

		this.mockMvc
				.perform(get("/api/v1/notifications").accept(MediaType.APPLICATION_JSON)
						.param("username", NotificationsDataCreator.USER_A_1).param("page", "0").param("size", "10"))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].subject", Matchers.is(NotificationsDataCreator.MESSAGE_SUBJECT)))
				.andExpect(jsonPath("$.content[0].read", is(true))).andReturn();
	}

}
