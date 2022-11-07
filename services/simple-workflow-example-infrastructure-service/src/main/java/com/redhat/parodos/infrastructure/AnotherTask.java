package com.redhat.parodos.infrastructure;

import com.redhat.parodos.infrastructure.task.InfrastructureTask;
import com.redhat.parodos.infrastructure.task.InfrastructureTaskAware;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * An example of a task. This one only logs
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Slf4j
public class AnotherTask implements InfrastructureTask, InfrastructureTaskAware {

	/**
	 * This is a simple example and only writes a Log
	 */
	public WorkReport execute(WorkContext workContext) {
		log.info("Executing another Task. This one does nothing...in practise this could open a Ticket in Jira, inject into Github. Sky is the limit");
		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}
}
