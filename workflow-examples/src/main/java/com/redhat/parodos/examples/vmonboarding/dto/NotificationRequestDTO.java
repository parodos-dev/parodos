package com.redhat.parodos.examples.vmonboarding.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequestDTO {

	private List<String> usernames;

	private String subject;

	private String body;

}
