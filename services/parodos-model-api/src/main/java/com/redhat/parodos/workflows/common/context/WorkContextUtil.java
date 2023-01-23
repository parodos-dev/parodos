package com.redhat.parodos.workflows.common.context;

import com.redhat.parodos.workflows.work.WorkContext;

public class WorkContextUtil {
    private static String spaceChar = " ";
    private static String underscoreChar = "_";

    public enum ProcessType {
        WORKFLOW_DEFINITION,
        WORKFLOW_TASK_DEFINITION,
        WORKFLOW_EXECUTION,
        WORKFLOW_TASK_EXECUTION
    }

    public enum Resource {
        ID,
        NAME,
        PARAMETERS,
        ARGUMENTS,
        STATUS,
        INFRASTRUCTURE_OPTIONS
    }

    public static String buildKey(ProcessType processType, Resource resource) {
        return String.format("%s%s%s",
                processType.name(),
                underscoreChar,
                resource.name()
        ).toUpperCase();
    }

    public static String buildKey(ProcessType processType, String processName, Resource resource) {
        return String.format("%s%s%s%s%s",
                processType.name(),
                underscoreChar,
                processName.replace(spaceChar, underscoreChar),
                underscoreChar,
                resource.name()
        ).toUpperCase();
    }

    public static Object read(WorkContext workContext, ProcessType processType, String processName, Resource resource) {
        return workContext.get(buildKey(processType, processName, resource));
    }

    public static void write(WorkContext workContext, ProcessType processType, String processName, Resource resource, Object object) {
        workContext.put(buildKey(processType, processName, resource), object);
    }

    public static Object read(WorkContext workContext, ProcessType processType, Resource resource) {
        return workContext.get(buildKey(processType, resource));
    }

    public static void write(WorkContext workContext, ProcessType processType, Resource resource, Object object) {
        workContext.put(buildKey(processType, resource), object);
    }
}
