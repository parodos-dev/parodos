package com.redhat.parodos.workflow.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotatedTypeMetadata;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

class BeanWorkFlowRegistryImplTest {

	private static final String TASK_DEPENDENCY = "task_dependency";

	private static final String FOO_DEPENDENCY = "foo_dependency";

	private static final String TEST_TASK = "test-task";

	private static final String TEST = "test";

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
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory,
				this.workFlowDefinitionService, new HashMap<>(), new HashMap<>());
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(0)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(0)).save(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
	}

	@Test
	void TestRegistryWithValidWorkflows() {

		// given
		Work work = Mockito.mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		Mockito.when(this.beanFactory.getDependenciesForBean(Mockito.eq(TEST)))
				.thenReturn(new String[] { FOO_DEPENDENCY });
		AnnotatedTypeMetadata bean = Mockito.mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
		Mockito.when(beanDefinition.getSource()).thenReturn(bean);
		Mockito.when(this.beanFactory.getBeanDefinition(Mockito.any())).thenReturn(beanDefinition);

		// when
		@SuppressWarnings("serial")
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory,
				this.workFlowDefinitionService, new HashMap<String, WorkFlowTask>(), new HashMap<String, WorkFlow>() {
					{
						put(TEST, workFlow);
					}
				});
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).save(Mockito.eq(TEST), Mockito.eq(TEST),
				Mockito.eq(WorkFlowType.ASSESSMENT), Mockito.argThat(argument -> {
					if (argument instanceof HashMap) {
						HashMap<?, ?> data = ((HashMap<?, ?>) argument);
						if (data.isEmpty()) {
							return false;
						}
						return data.containsKey(FOO_DEPENDENCY);
					}
					return false;
				}));
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
		@SuppressWarnings("serial")
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory,
				this.workFlowDefinitionService, new HashMap<String, WorkFlowTask>() {
					{
						put(TEST_TASK, null);
					}
				}, new HashMap<String, WorkFlow>() {
					{
						put(TEST, workFlow);
					}
				});
		registry.postInit();

		// then
		Mockito.verify(this.beanFactory, Mockito.times(2)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).save(Mockito.eq(TEST), Mockito.eq(TEST),
				Mockito.eq(WorkFlowType.ASSESSMENT), Mockito.argThat(argument -> {
					if (argument instanceof HashMap) {
						HashMap<?, ?> data = ((HashMap<?, ?>) argument);
						if (data.isEmpty()) {
							return false;
						}
						return data.containsKey(FOO_DEPENDENCY);
					}
					return false;
				}));

		Mockito.verify(this.workFlowDefinitionService, Mockito.times(1)).saveWorkFlowChecker(Mockito.eq(TEST_TASK),
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
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory,
				this.workFlowDefinitionService, new HashMap<String, WorkFlowTask>(), new HashMap<String, WorkFlow>() {
					{
						put(TEST, workFlow);
					}
				});
		Exception exception = assertThrows(RuntimeException.class, registry::postInit);

		// then
		assertNotNull(exception);

		Mockito.verify(this.beanFactory, Mockito.times(1)).getDependenciesForBean(Mockito.any());
		Mockito.verify(this.workFlowDefinitionService, Mockito.times(0)).save(Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any());
		assertEquals(registry.getWorkFlowByName(TEST), workFlow);
	}

}