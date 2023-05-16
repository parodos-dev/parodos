package com.redhat.parodos.workflow.execution.scheduler.model;

import java.util.UUID;

public record ScheduledTaskKey(UUID projectId, UUID userId) {
}
