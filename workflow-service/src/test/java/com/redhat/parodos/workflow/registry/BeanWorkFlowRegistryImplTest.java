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

	private static final String TEST = "test";

	private static final String TEST_TASK = "test-task";

	private static final String TASK_DEPENDENCY = "task_dependency";

	private static final String FOO_DEPENDENCY = "foo_dependency";

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
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.any()))
				.thenReturn(new String[] { FOO_DEPENDENCY });
		AnnotatedTypeMetadata bean = Mockito.mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(beanDefinition.getSource()).thenReturn(bean);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put(TEST, workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);

		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());

		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).save(Mockito.eq(TEST),
				Mockito.eq(WorkFlowType.ASSESSMENT), Mockito.anyList(), Mockito.anyList(),
				Mockito.eq(WorkFlowProcessingType.SEQUENTIAL));

		assertEquals(registry.getWorkFlowByName(TEST), workFlow);
	}

	@Test
	void TestRegistryWithValidWorkflowTasks() {
		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq(TEST)))
				.thenReturn(new String[] { FOO_DEPENDENCY });
		AnnotatedTypeMetadata bean = Mockito.mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(beanDefinition.getSource()).thenReturn(bean);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq(TEST_TASK)))
				.thenReturn(new String[] { TASK_DEPENDENCY });

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put(TEST, workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).save(Mockito.eq("test"),
				Mockito.eq(WorkFlowType.ASSESSMENT), Mockito.anyList(), Mockito.anyList(),
				Mockito.eq(WorkFlowProcessingType.SEQUENTIAL));

		Mockito.verify(this.workFlowDefinitionService, Mockito.times(0)).saveWorkFlowChecker(Mockito.eq(TEST_TASK),
				Mockito.eq(TASK_DEPENDENCY), Mockito.any());
	}

	@SuppressWarnings("serial")
	@Test
	void TestThatBeanIsAnnotatedTypeMetadata() {

		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq(TEST)))
				.thenReturn(new String[] { FOO_DEPENDENCY });

		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put(TEST, workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);
		Exception exception = assertThrows(RuntimeException.class, registry::postInit);

		// then
		assertNotNull(exception);

		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(0)).save(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any());
		assertEquals(registry.getWorkFlowByName(TEST), workFlow);
	}

}