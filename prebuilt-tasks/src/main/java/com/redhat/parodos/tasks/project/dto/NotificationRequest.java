package com.redhat.parodos.tasks.project.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {

	private List<String> usernames;

	private String subject;

	private String body;

}
