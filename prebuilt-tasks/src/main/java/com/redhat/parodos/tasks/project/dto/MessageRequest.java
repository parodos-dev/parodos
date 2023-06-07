package com.redhat.parodos.tasks.project.dto;

import java.util.List;

public record MessageRequest(String name, List<String> recipients, String message, String siteName) {
}
