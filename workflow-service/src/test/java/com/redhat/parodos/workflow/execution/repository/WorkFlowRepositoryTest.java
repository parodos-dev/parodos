package com.redhat.parodos.workflow.execution.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.repository.RepositoryTestBase;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkFlowRepositoryTest extends RepositoryTestBase {

	@Test
	public void testFindAll() {
		// given
		createWorkFlowExecution();
		createWorkFlowExecution();

		// when
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository.findAll();

		// then
		assertNotNull(workFlowExecutions);
		assertEquals(2, workFlowExecutions.size());
	}

	@Test
	public void testSave() {
		// given
		User user = createUser();
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(createWorkFlowDefinition())
				.status(WorkStatus.IN_PROGRESS).projectId(createProject(user).getId()).build();
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository.findAll();
		assertTrue(workFlowExecutions.isEmpty());

		// when
		WorkFlowExecution flowExecution = workFlowRepository.save(workFlowExecution);

		// then
		flowExecution = workFlowRepository.getById(workFlowExecution.getId());
		assertNotNull(flowExecution);
		assertNotNull(flowExecution.getId());
		assertEquals(workFlowExecution.getWorkFlowDefinition(), flowExecution.getWorkFlowDefinition());
		assertEquals(workFlowExecution.getProjectId(), flowExecution.getProjectId());
		assertEquals(WorkStatus.IN_PROGRESS, flowExecution.getStatus());
	}

	@Test
	public void testSaveWithExecutionContext() {
		// given
		WorkFlowExecution workFlowExecution = createWorkFlowExecution();
		workFlowExecution = workFlowRepository.save(workFlowExecution);
		WorkContext WorkContext = new WorkContext();
		WorkContext.put("test_key", "test_value");
		UUID testUUID = UUID.randomUUID();
		WorkContext.put("test_uuid", testUUID);
		List<String> testList = List.of("test1", "test2");
		WorkContext.put("test_list", testList);
		WorkFlowExecutionContext workContext = WorkFlowExecutionContext.builder()
				.mainWorkFlowExecution(workFlowExecution).workContext(WorkContext).build();
		workFlowExecution.setWorkFlowExecutionContext(workContext);

		// when
		workFlowRepository.save(workFlowExecution);

		// then
		WorkFlowExecution flowExecution = workFlowRepository.getById(workFlowExecution.getId());
		assertNotNull(flowExecution);
		assertNotNull(flowExecution.getWorkFlowExecutionContext());
		assertNotNull(flowExecution.getWorkFlowExecutionContext().getWorkContext());
		assertEquals("test_value", flowExecution.getWorkFlowExecutionContext().getWorkContext().get("test_key"));
		assertEquals(testList, flowExecution.getWorkFlowExecutionContext().getWorkContext().get("test_list"));
		assertEquals(testUUID, flowExecution.getWorkFlowExecutionContext().getWorkContext().get("test_uuid"));
	}

	@Test
	public void testFindByMainWorkFlowExecution() {
		// given
		User user = createUser();
		WorkFlowExecution mainWorkFlowExecution = createWorkFlowExecution();
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(createWorkFlowDefinition())
				.status(WorkStatus.IN_PROGRESS).projectId(createProject(user).getId()).user(createUser())
				.mainWorkFlowExecution(mainWorkFlowExecution).build();
		workFlowExecution = workFlowRepository.save(workFlowExecution);

		// when
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository
				.findByMainWorkFlowExecution(mainWorkFlowExecution);

		// then
		assertNotNull(workFlowExecutions);
		assertEquals(1, workFlowExecutions.size());
		assertEquals(workFlowExecution, workFlowExecutions.get(0));
	}

	@Test
	public void testFindByStatusInAndIsMain() {
		// given
		User user = createUser();
		WorkFlowExecution mainWorkFlowExecution = createWorkFlowExecution();
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(createWorkFlowDefinition())
				.status(WorkStatus.IN_PROGRESS).projectId(createProject(user).getId()).user(createUser())
				.mainWorkFlowExecution(mainWorkFlowExecution).build();
		workFlowRepository.save(workFlowExecution);

		// when
		List<WorkFlowExecution> workFlowExecutions = workFlowRepository
				.findByStatusInAndIsMain(List.of(WorkStatus.IN_PROGRESS));

		// then
		assertNotNull(workFlowExecutions);
		assertEquals(1, workFlowExecutions.size());
		assertEquals(mainWorkFlowExecution, workFlowExecutions.get(0));
	}

	@Test
	public void testCountRestartedWorkflow() {
		// given
		User user = createUser();
		WorkFlowExecution mainWorkFlowExecution = createWorkFlowExecution();
		WorkFlowDefinition workFlowDefinition = createWorkFlowDefinition();
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(workFlowDefinition)
				.status(WorkStatus.IN_PROGRESS).projectId(createProject(user).getId()).user(createUser())
				.mainWorkFlowExecution(mainWorkFlowExecution).build();
		workFlowRepository.save(workFlowExecution);

		// when
		int count = workFlowRepository.countRestartedWorkflow(workFlowExecution.getId());

		// then
		assertEquals(0, count);

		// When
		WorkFlowExecution restartedWorkFlowExecution = WorkFlowExecution.builder()
				.workFlowDefinition(workFlowDefinition).status(WorkStatus.IN_PROGRESS)
				.projectId(createProject(user).getId()).user(createUser()).mainWorkFlowExecution(mainWorkFlowExecution)
				.originalWorkFlowExecution(workFlowExecution).build();
		workFlowRepository.save(restartedWorkFlowExecution);

		count = workFlowRepository.countRestartedWorkflow(workFlowExecution.getId());
		int countForRestarted = workFlowRepository.countRestartedWorkflow(restartedWorkFlowExecution.getId());

		// then
		assertEquals(1, count);
		assertEquals(0, countForRestarted);
	}

	private User createUser() {
		User user = User.builder().username(UUID.randomUUID().toString()).build();
		return entityManager.persist(user);
	}

	private Project createProject(User user) {
		Project project = Project.builder().name(UUID.randomUUID().toString()).build();
		project.setCreatedDate(new Date());
		project.setCreatedBy(user.getId());
		return entityManager.persist(project);
	}

	private WorkFlowDefinition createWorkFlowDefinition() {
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(UUID.randomUUID().toString())
				.numberOfWorks(1).parameters("{}").build();
		return entityManager.persist(workFlowDefinition);
	}

	private WorkFlowExecution createWorkFlowExecution() {
		User user = createUser();
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinition(createWorkFlowDefinition())
				.status(WorkStatus.IN_PROGRESS).projectId(createProject(user).getId()).user(createUser()).build();
		return entityManager.persist(workFlowExecution);
	}

}
