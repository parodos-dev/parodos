package com.redhat.parodos.workflow.registry;

import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanWorkFlowRegistryImplTest {

	private ConfigurableListableBeanFactory beanFactory;

	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	@BeforeEach
	public void initEach() {
		this.beanFactory = Mockito.mock(ConfigurableListableBeanFactory.class);
		this.workFlowDefinitionService = Mockito.mock(WorkFlowDefinitionServiceImpl.class);
	}

	@Test
	void simpleTestWithoutWorkflows() {
		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>(),
				new HashMap<>(), this.workFlowDefinitionService);
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(0)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(0)).save(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void TestRegistryWithValidWorkflows() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq("test")))
				.thenReturn(new String[] { "foo_dependency" });
		AnnotatedTypeMetadata bean = Mockito.mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(beanDefinition.getSource()).thenReturn(bean);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put("test", workFlow);
			}
		}, new HashMap<>() {
			{
				put("test-task", null);
			}
		}, this.workFlowDefinitionService);
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).save(Mockito.eq("test"),
				Mockito.eq(WorkFlowType.ASSESSMENT), Mockito.argThat(arguments -> {
					if (arguments != null) {
						if (arguments.isEmpty()) {
							return false;
						}
						return arguments.contains("foo_dependency");
					}
					return false;
				}), Mockito.argThat(works -> {
					if (works != null) {
						if (works.isEmpty()) {
							return false;
						}
						return works.contains("foo_dependency");
					}
					return false;
				}), Mockito.eq(WorkFlowProcessingType.SEQUENTIAL));
		assertEquals(registry.getWorkFlowByName("test"), workFlow);
	}

	@Test
	void TestRegistryWithValidWorkflowTasks() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq("test")))
				.thenReturn(new String[] { "foo_dependency" });
		AnnotatedTypeMetadata bean = Mockito.mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(beanDefinition.getSource()).thenReturn(bean);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq("test-task")))
				.thenReturn(new String[] { "task_dependency" });

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put("test", workFlow);
			}
		}, new HashMap<>() {
			{
				put("test-task", null);
			}
		}, this.workFlowDefinitionService);
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(2)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).save(Mockito.eq("test"),
				Mockito.eq(WorkFlowType.ASSESSMENT), Mockito.argThat(arguments -> {
					if (arguments != null) {
						if (arguments.isEmpty()) {
							return false;
						}
						return arguments.contains("foo_dependency");
					}
					return false;
				}), Mockito.argThat(works -> {
					if (works != null) {
						if (works.isEmpty()) {
							return false;
						}
						return works.contains("foo_dependency");
					}
					return false;
				}), Mockito.eq(WorkFlowProcessingType.SEQUENTIAL));

		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).saveWorkFlowChecker(Mockito.eq("test-task"),
				Mockito.eq("task_dependency"), Mockito.any());
	}

	@Test
	void TestThatBeanIsAnnotatedTypeMetadata() {

		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq("test")))
				.thenReturn(new String[] { "foo_dependency" });

		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put("test", workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);
		Exception exception = assertThrows(RuntimeException.class, registry::postInit);

		// then
		assertNotNull(exception);
		assertEquals(exception.getMessage(), "workflow with no annotated type metadata!");

		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(0)).save(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any());
		assertEquals(registry.getWorkFlowByName("test"), workFlow);
	}

}