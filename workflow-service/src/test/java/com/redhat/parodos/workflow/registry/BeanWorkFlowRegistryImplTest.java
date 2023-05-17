package com.redhat.parodos.workflow.registry;

import java.util.HashMap;
import java.util.Map;

import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.enums.WorkFlowProcessingType;
import com.redhat.parodos.workflow.enums.WorkFlowType;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeanWorkFlowRegistryImplTest {

	private static final String TEST = "test";

	private static final String TEST_TASK = "test-task";

	private static final String TASK_DEPENDENCY = "task_dependency";

	private static final String FOO_DEPENDENCY = "foo_dependency";

	private ConfigurableListableBeanFactory beanFactory;

	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	@BeforeEach
	public void initEach() {
		this.beanFactory = mock(ConfigurableListableBeanFactory.class);
		this.workFlowDefinitionService = mock(WorkFlowDefinitionServiceImpl.class);
	}

	@Test
	void simpleTestWithoutWorkflows() {
		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>(),
				new HashMap<>(), this.workFlowDefinitionService);
		registry.postInit();

		// then
		verify(this.beanFactory, times(0)).getDependenciesForBean(any());
		verify(this.workFlowDefinitionService, times(0)).save(any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void TestRegistryWithValidWorkflows() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		when(this.beanFactory.getDependenciesForBean(any())).thenReturn(new String[] { FOO_DEPENDENCY });
		AnnotatedTypeMetadata bean = mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = mock(BeanDefinition.class);
		when(beanDefinition.getSource()).thenReturn(bean);
		when(this.beanFactory.getBeanDefinition(any())).thenReturn(beanDefinition);

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put(TEST, workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);

		registry.postInit();

		// then
		verify(this.beanFactory, times(1)).getDependenciesForBean(any());

		verify(this.workFlowDefinitionService, times(1)).save(eq(TEST), eq(WorkFlowType.ASSESSMENT), any(), anyList(),
				anyList(), eq(WorkFlowProcessingType.SEQUENTIAL), nullable(String.class));

		assertEquals(registry.getWorkFlowByName(TEST), workFlow);
	}

	@Test
	void TestRegistryWithValidWorkflowTasks() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		when(this.beanFactory.getDependenciesForBean(eq(TEST))).thenReturn(new String[] { FOO_DEPENDENCY });
		AnnotatedTypeMetadata bean = mock(AnnotatedTypeMetadata.class);
		BeanDefinition beanDefinition = mock(BeanDefinition.class);
		when(beanDefinition.getSource()).thenReturn(bean);
		when(this.beanFactory.getBeanDefinition(any())).thenReturn(beanDefinition);
		when(bean.getAnnotationAttributes(any())).thenReturn(Map.of());
		when(this.beanFactory.getDependenciesForBean(eq(TEST_TASK))).thenReturn(new String[] { TASK_DEPENDENCY });

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put(TEST, workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);
		registry.postInit();

		// then
		verify(this.beanFactory, times(1)).getDependenciesForBean(any());
		verify(this.workFlowDefinitionService, times(1)).save(eq("test"), eq(WorkFlowType.ASSESSMENT), any(), anyList(),
				anyList(), eq(WorkFlowProcessingType.SEQUENTIAL), nullable(String.class));

		verify(this.workFlowDefinitionService, times(0)).saveWorkFlowChecker(eq(TEST_TASK), eq(TASK_DEPENDENCY), any());
	}

	@SuppressWarnings("serial")
	@Test
	void TestThatBeanIsAnnotatedTypeMetadata() {

		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named(TEST).execute(work).build();

		when(this.beanFactory.getDependenciesForBean(eq(TEST))).thenReturn(new String[] { FOO_DEPENDENCY });

		BeanDefinition beanDefinition = mock(BeanDefinition.class);
		when(this.beanFactory.getBeanDefinition(any())).thenReturn(beanDefinition);

		// when
		BeanWorkFlowRegistryImpl registry = new BeanWorkFlowRegistryImpl(this.beanFactory, new HashMap<>() {
			{
				put(TEST, workFlow);
			}
		}, new HashMap<>(), this.workFlowDefinitionService);
		Exception exception = assertThrows(RuntimeException.class, registry::postInit);

		// then
		assertNotNull(exception);

		verify(this.workFlowDefinitionService, times(0)).save(any(), any(), any(), any(), any(), any(), any());
		assertEquals(registry.getWorkFlowByName(TEST), workFlow);
	}

}
