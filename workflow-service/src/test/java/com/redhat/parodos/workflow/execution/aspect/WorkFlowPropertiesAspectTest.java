package com.redhat.parodos.workflow.execution.aspect;

import com.redhat.parodos.workflow.annotation.WorkFlowProperties;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import com.redhat.parodos.workflows.workflow.WorkFlowPropertiesMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

class WorkFlowPropertiesAspectTest {

	@Test
	public void testWorkFlowPropertiesAspectWithValidData() {
		// given
		WorkFlow workflow = Mockito.mock(WorkFlow.class);
		WorkFlowProperties properties = Mockito.mock(WorkFlowProperties.class);
		Mockito.when(properties.version()).thenReturn("1.0.0");
		Environment env = Mockito.mock(Environment.class);
		Mockito.when(env.resolvePlaceholders(Mockito.anyString())).thenReturn("1.0.0");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setProperitesForWorkflow(workflow, properties);
			assertNotNull(result);
		});

		// then
		ArgumentCaptor<WorkFlowPropertiesMetadata> argument = ArgumentCaptor.forClass(WorkFlowPropertiesMetadata.class);
		Mockito.verify(workflow, Mockito.times(1)).setProperties(argument.capture());
		assertEquals(argument.getValue().getVersion(), "1.0.0");
		Mockito.verify(env, Mockito.times(1)).resolvePlaceholders("1.0.0");
	}

	@Test
	public void testWorkFlowPropertiesAspectWithValidEnvData() {
		// given
		WorkFlow workflow = Mockito.mock(WorkFlow.class);
		WorkFlowProperties properties = Mockito.mock(WorkFlowProperties.class);
		Mockito.when(properties.version()).thenReturn("${git.commit.id}");
		Environment env = Mockito.mock(Environment.class);
		Mockito.when(env.resolvePlaceholders(Mockito.anyString())).thenReturn("1.0.0");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;


		assertDoesNotThrow(() -> {
			Object result = aspect.setProperitesForWorkflow(workflow, properties);
			assertNotNull(result);
		});

		// then
		ArgumentCaptor<WorkFlowPropertiesMetadata> argument = ArgumentCaptor.forClass(WorkFlowPropertiesMetadata.class);
		Mockito.verify(workflow, Mockito.times(1)).setProperties(argument.capture());
		assertEquals(argument.getValue().getVersion(), "1.0.0");
		Mockito.verify(env, Mockito.times(1)).resolvePlaceholders("${git.commit.id}");
	}


	@Test
	public void testWorkFlowPropertiesAspectWithNoValidData() {
		// given
		WorkFlow workflow = Mockito.mock(WorkFlow.class);
		WorkFlowProperties properties = Mockito.mock(WorkFlowProperties.class);
		Mockito.when(properties.version()).thenReturn("1.0.0");
		Environment env = Mockito.mock(Environment.class);
		Mockito.when(env.resolvePlaceholders(Mockito.anyString())).thenReturn("1.0.0");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setProperitesForWorkflow(workflow, null);
			assertNotNull(result);
		});

		// then
		Mockito.verify(workflow, Mockito.times(0)).setProperties(Mockito.any());
	}

	@Test
	public void testWorkFlowPropertiesAspectWithNoValidVersion() {
		// given

		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		workFlow.setProperties(WorkFlowPropertiesMetadata.builder().version("1.0.0").build());

		WorkFlowProperties properties = Mockito.mock(WorkFlowProperties.class);
		Mockito.when(properties.version()).thenReturn("1.0.0");
		Environment env = Mockito.mock(Environment.class);
		Mockito.when(env.resolvePlaceholders(Mockito.anyString())).thenReturn("");

		// when
		WorkFlowPropertiesAspect aspect = new WorkFlowPropertiesAspect();
		aspect.env = env;

		assertDoesNotThrow(() -> {
			Object result = aspect.setProperitesForWorkflow(workFlow, properties);
			assertNotNull(result);
		});

		// then
		assertNotEquals(workFlow.getProperties().getVersion(), "1.0.0");
	}

}