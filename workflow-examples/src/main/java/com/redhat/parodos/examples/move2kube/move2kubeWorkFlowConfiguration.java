package com.redhat.parodos.examples.move2kube;

import java.util.Arrays;
import java.util.concurrent.Executors;

import com.redhat.parodos.examples.move2kube.checker.TransformChecker;
import com.redhat.parodos.examples.move2kube.task.GitArchiveTask;
import com.redhat.parodos.examples.move2kube.task.GitBranchTask;
import com.redhat.parodos.examples.move2kube.task.GitCommitTask;
import com.redhat.parodos.examples.move2kube.task.Move2KubePlan;
import com.redhat.parodos.examples.move2kube.task.Move2KubeRetrieve;
import com.redhat.parodos.examples.move2kube.task.Move2KubeTask;
import com.redhat.parodos.examples.move2kube.task.Move2KubeTransform;
import com.redhat.parodos.tasks.git.GitCloneTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class move2kubeWorkFlowConfiguration {

	@Bean
	GitCloneTask gitCloneTask() {
		return new GitCloneTask();
	}

	@Bean
	GitArchiveTask gitArchiveTask() {
		return new GitArchiveTask();
	}

	@Bean
	GitCommitTask gitCommitTask() {
		return new GitCommitTask();
	}

	@Bean
	GitBranchTask gitBranchTask() {
		return new GitBranchTask();
	}

	private String getMove2KubeAPIEndpoint() {
		return "http://localhost:8081/api/v1";
	}

	@Bean
	Move2KubeTask move2KubeTask() {
		return new Move2KubeTask(getMove2KubeAPIEndpoint());
	}

	@Bean
	Move2KubeRetrieve move2KubeRetrieve() {
		return new Move2KubeRetrieve(getMove2KubeAPIEndpoint());
	}

	@Bean
	TransformChecker transformChecker() {
		return new TransformChecker(getMove2KubeAPIEndpoint());
	}

	@Bean(name = "transformWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow transformWorkFlowChecker(@Qualifier("transformChecker") TransformChecker transformChecker) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("transformWorkFlowChecker").execute(transformChecker)
				.build();
	}

	@Bean
	Move2KubeTransform move2KubeTransform(@Qualifier("transformWorkFlowChecker") WorkFlow transformWorkFlowChecker) {
		Move2KubeTransform move2KubeTransform = new Move2KubeTransform(getMove2KubeAPIEndpoint());
		move2KubeTransform.setWorkFlowCheckers(Arrays.asList(transformWorkFlowChecker));
		return move2KubeTransform;
	}

	@Bean
	Move2KubePlan move2KubePlan() {
		return new Move2KubePlan(getMove2KubeAPIEndpoint());
	}

	@Bean(name = "move2KubeProject")
	@Infrastructure
	WorkFlow move2KubeProject(@Qualifier("move2KubeTask") Move2KubeTask move2KubeTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("move2KubeProject").execute(move2KubeTask).build();
	}

	@Bean(name = "getSources")
	@Infrastructure
	WorkFlow getSources(@Qualifier("gitCloneTask") GitCloneTask gitCloneTask,
			@Qualifier("gitArchiveTask") GitArchiveTask gitArchiveTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("getSources").execute(gitCloneTask)
				.then(gitArchiveTask).build();
	}

	@Bean(name = "preparationWorkflow")
	@Infrastructure
	WorkFlow preparationWorkflow(@Qualifier("getSources") WorkFlow getSources,
			@Qualifier("move2KubeProject") WorkFlow move2KubeProject) {
		return ParallelFlow.Builder.aNewParallelFlow().named("preparationWorkflow")
				.execute(move2KubeProject, getSources).with(Executors.newFixedThreadPool(2)).build();
	}

	@Bean(name = "move2KubeWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW)
	@Infrastructure
	WorkFlow move2kubeWorkflow(@Qualifier("preparationWorkflow") WorkFlow preparationWorkflow,
			@Qualifier("move2KubePlan") Move2KubePlan move2KubePlan,
			@Qualifier("move2KubeTransform") Move2KubeTransform move2KubeTransform,
			@Qualifier("gitBranchTask") GitBranchTask gitBranchTask,
			@Qualifier("move2KubeRetrieve") Move2KubeRetrieve move2KubeRetrieve,
			@Qualifier("gitCommitTask") GitCommitTask gitCommitTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("move2KubeWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW).execute(preparationWorkflow)
				.then(move2KubePlan).then(move2KubeTransform).then(gitBranchTask).then(move2KubeRetrieve)
				.then(gitCommitTask).build();
	}

}
