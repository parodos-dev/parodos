package com.redhat.parodos.tasks.project.dto;

public record MessageRequest(String username, String subject, String[] recipients, String message) {
}
