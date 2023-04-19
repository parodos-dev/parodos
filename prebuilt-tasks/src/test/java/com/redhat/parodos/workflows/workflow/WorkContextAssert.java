package com.redhat.parodos.workflows.workflow;

import com.redhat.parodos.workflows.work.WorkContext;
import org.assertj.core.api.AbstractAssert;

public class WorkContextAssert extends AbstractAssert<WorkContextAssert, WorkContext> {

	private WorkContextAssert(WorkContext workContext, Class<?> selfType) {
		super(workContext, selfType);
	}

	public static WorkContextAssert assertThat(WorkContext actual) {
		return new WorkContextAssert(actual, WorkContextAssert.class);
	}

	public WorkContextAssert hasEntryKey(String name) {
		isNotNull();
		if (actual.get(name) == null) {
			failWithMessage("Expected WorkContext to contain key <%s> but got keys <%s>", name,
					actual.getEntrySet().stream().map(e -> e.getKey()).toList());
		}
		return this;
	}

}
