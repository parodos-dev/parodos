package com.redhat.parodos.examples.move2kube;

import java.util.List;
import java.util.concurrent.Executors;

import com.redhat.parodos.examples.move2kube.checker.TransformChecker;
import com.redhat.parodos.examples.move2kube.task.GitArchiveTask;
import com.redhat.parodos.examples.move2kube.task.GitBranchTask;
import com.redhat.parodos.examples.move2kube.task.GitCommitTask;
import com.redhat.parodos.examples.move2kube.task.GitPushTask;
import com.redhat.parodos.examples.move2kube.task.Move2KubePlan;
import com.redhat.parodos.examples.move2kube.task.Move2KubeRetrieve;
import com.redhat.parodos.examples.move2kube.task.Move2KubeTask;
import com.redhat.parodos.examples.move2kube.task.Move2KubeTransform;
import com.redhat.parodos.infrastructure.Notifier;
import com.redhat.parodos.tasks.git.GitCloneTask;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.consts.WorkFlowConstants;
import com.redhat.parodos.workflows.workflow.ParallelFlow;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import com.redhat.parodos.workflows.workflow.WorkFlow;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class move2kubeWorkFlowConfiguration {

	@Value("${workflows.m2k.url}")
	private String m2kURL;

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
	GitPushTask gitPushTask() {
		return new GitPushTask();
	}

	@Bean
	GitBranchTask gitBranchTask() {
		return new GitBranchTask();
	}

	@Bean
	Move2KubeTask move2KubeTask() {
		return new Move2KubeTask(m2kURL);
	}

	@Bean
	Move2KubeRetrieve move2KubeRetrieve() {
		return new Move2KubeRetrieve(m2kURL);
	}

	@Bean
	TransformChecker transformChecker() {
		return new TransformChecker(m2kURL);
	}

	@Bean(name = "transformWorkFlowChecker")
	@Checker(cronExpression = "*/5 * * * * ?")
	WorkFlow transformWorkFlowChecker(@Qualifier("transformChecker") TransformChecker transformChecker) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("transformWorkFlowChecker").execute(transformChecker)
				.build();
	}

	@Bean
	Move2KubeTransform move2KubeTransform(@Qualifier("transformWorkFlowChecker") WorkFlow transformWorkFlowChecker,
			Notifier notifier) {
		Move2KubeTransform move2KubeTransform = new Move2KubeTransform(m2kURL, notifier);
		move2KubeTransform.setWorkFlowCheckers(List.of(transformWorkFlowChecker));
		return move2KubeTransform;
	}

	@Bean
	Move2KubePlan move2KubePlan() {
		return new Move2KubePlan(m2kURL);
	}

	@Bean(name = "move2KubeProject")
	@Infrastructure
	WorkFlow move2KubeProject(@Qualifier("move2KubeTask") Move2KubeTask move2KubeTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("move2KubeProject").execute(move2KubeTask).build();
	}

	@Bean(name = "getSources")
	@Infrastructure
	WorkFlow getSources(@Qualifier("gitCloneTask") GitCloneTask gitCloneTask,
			@Qualifier("gitBranchTask") GitBranchTask gitBranchTask,
			@Qualifier("gitArchiveTask") GitArchiveTask gitArchiveTask) {
		return SequentialFlow.Builder.aNewSequentialFlow().named("getSources").execute(gitCloneTask).then(gitBranchTask)
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
			@Qualifier("move2KubeRetrieve") Move2KubeRetrieve move2KubeRetrieve,
			@Qualifier("gitCommitTask") GitCommitTask gitCommitTask,
			@Qualifier("gitPushTask") GitPushTask gitPushTask) {
		return SequentialFlow.Builder.aNewSequentialFlow()
				.named("move2KubeWorkFlow" + WorkFlowConstants.INFRASTRUCTURE_WORKFLOW).execute(preparationWorkflow)
				.then(move2KubePlan).then(move2KubeTransform).then(move2KubeRetrieve).then(gitCommitTask)
				.then(gitPushTask).build();
	}

}
