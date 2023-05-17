package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.annotation.WorkFlowProperties;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkFlowPropertiesAspectTest {

	@Test
	public void testWorkFlowPropertiesAspectWithValidData() {
		// given
		WorkFlow workflow = mock(WorkFlow.class);
		WorkFlowProperties properties = mock(WorkFlowProperties.class);
		when(properties.version()).thenReturn("1.0.0");
		Environment env = mock(Environment.class);
		when(env.resolvePlaceholders(anyString())).thenReturn("1.0.0");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setPropertiesForWorkflow(workflow, properties);
			assertNotNull(result);
		});

		// then
		ArgumentCaptor<WorkFlowPropertiesMetadata> argument = ArgumentCaptor.forClass(WorkFlowPropertiesMetadata.class);
		verify(workflow, times(1)).setProperties(argument.capture());
		assertEquals(argument.getValue().getVersion(), "1.0.0");
		verify(env, times(1)).resolvePlaceholders("1.0.0");
	}

	@Test
	public void testWorkFlowPropertiesAspectWithValidEnvData() {
		// given
		WorkFlow workflow = mock(WorkFlow.class);
		WorkFlowProperties properties = mock(WorkFlowProperties.class);
		when(properties.version()).thenReturn("${git.commit.id}");
		Environment env = mock(Environment.class);
		when(env.resolvePlaceholders(anyString())).thenReturn("1.0.0");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setPropertiesForWorkflow(workflow, properties);
			assertNotNull(result);
		});

		// then
		ArgumentCaptor<WorkFlowPropertiesMetadata> argument = ArgumentCaptor.forClass(WorkFlowPropertiesMetadata.class);
		verify(workflow, times(1)).setProperties(argument.capture());
		assertEquals(argument.getValue().getVersion(), "1.0.0");
		verify(env, times(1)).resolvePlaceholders("${git.commit.id}");
	}

	@Test
	public void testWorkFlowPropertiesAspectWithNoValidData() {
		// given
		WorkFlow workflow = mock(WorkFlow.class);
		WorkFlowProperties properties = mock(WorkFlowProperties.class);
		when(properties.version()).thenReturn("1.0.0");
		Environment env = mock(Environment.class);
		when(env.resolvePlaceholders(anyString())).thenReturn("1.0.0");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setPropertiesForWorkflow(workflow, null);
			assertNotNull(result);
		});

		// then
		verify(workflow, times(0)).setProperties(any());
	}

	@Test
	public void testWorkFlowPropertiesAspectWithNoValidVersion() {
		// given

		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		workFlow.setProperties(WorkFlowPropertiesMetadata.builder().version("1.0.0").build());

		WorkFlowProperties properties = mock(WorkFlowProperties.class);
		when(properties.version()).thenReturn("1.0.0");
		Environment env = mock(Environment.class);
		when(env.resolvePlaceholders(anyString())).thenReturn("");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setPropertiesForWorkflow(workFlow, properties);
			assertNotNull(result);
		});

		// then
		assertNotEquals(workFlow.getProperties().getVersion(), "1.0.0");
	}

}
