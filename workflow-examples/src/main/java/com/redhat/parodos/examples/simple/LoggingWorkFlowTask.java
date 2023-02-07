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
package com.redhat.parodos.examples.simple;

import com.redhat.parodos.workflow.task.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * logging task execution
 *
 * @author Luke Shannon (Github: lshannon)
 * @author Richard Wang (Github: richardw98)
 * @author Annel Ketcha (Github: anludke)
 */

@Slf4j
public class LoggingWorkFlowTask extends BaseInfrastructureWorkFlowTask {
    @Override
    public WorkReport execute(WorkContext workContext) {
        log.info(">>> Executing loggingWorkFlowTaskExecution");
        if (getGetWorkFlowChecker() != null) {
            log.info(">>> workflow task has a workflow checker");
        }

        log.info("Mocking a failed workflow checker task");
        return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
    }

    @Override
    public List<WorkFlowTaskParameter> getParameters() {
        return List.of(WorkFlowTaskParameter.builder()
                .key("api-server")
                .description("The api server")
                .type(WorkFlowTaskParameterType.URL)
                .optional(false)
                .build());
    }

    @Override
    public List<WorkFlowTaskOutput> getOutputs() {
        return List.of(WorkFlowTaskOutput.OTHER);
    }
}
