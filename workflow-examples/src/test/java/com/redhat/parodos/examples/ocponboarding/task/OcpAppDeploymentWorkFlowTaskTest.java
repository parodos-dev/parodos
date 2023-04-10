package com.redhat.parodos.examples.ocponboarding.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.ocponboarding.task.dto.notification.NotificationRequest;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.dsl.AppsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.ProjectRequestOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Ocp App Deployment Workflow Task execution test
 *
 * @author Annel Ketcha (GitHub: anludke)
 */

public class OcpAppDeploymentWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

    private static final String CLUSTER_API_URL_TEST = "cluster-api-url-test";

    private static final String NAMESPACE_KEY_TEST = "NAMESPACE";

    private static final String NAMESPACE_VALUE_TEST = "namespace-value-test";

    private static final String CLUSTER_TOKEN_KEY_TEST = "CLUSTER_TOKEN";

    private static final String CLUSTER_TOKEN_VALUE_TEST = "cluster-token-value-test";

    private OcpAppDeploymentWorkFlowTask ocpAppDeploymentWorkFlowTask;

    @Before
    public void setUp() {
        this.ocpAppDeploymentWorkFlowTask = spy((OcpAppDeploymentWorkFlowTask) getConcretePersonImplementation());
        try {
            doReturn(NAMESPACE_VALUE_TEST).when(this.ocpAppDeploymentWorkFlowTask)
                    .getRequiredParameterValue(Mockito.any(WorkContext.class), eq(NAMESPACE_KEY_TEST));

            doReturn(CLUSTER_TOKEN_VALUE_TEST).when(this.ocpAppDeploymentWorkFlowTask)
                    .getRequiredParameterValue(Mockito.any(WorkContext.class), eq(CLUSTER_TOKEN_KEY_TEST));
        }
        catch (MissingParameterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
        return new OcpAppDeploymentWorkFlowTask(CLUSTER_API_URL_TEST);
    }

    @Test
    public void executeSuccess() {
        WorkContext workContext = Mockito.mock(WorkContext.class);
        ProjectRequestOperation projectRequestOperation = Mockito.mock(ProjectRequestOperation.class);
        ProjectRequest projectRequest = Mockito.mock(ProjectRequest.class);
        AppsAPIGroupDSL appsAPIGroupDSL = Mockito.mock(AppsAPIGroupDSL.class);
        MixedOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>> mixedOperationDeployment = Mockito.mock(MixedOperation.class);
        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        RollableScalableResource rollableScalableResource = Mockito.mock(RollableScalableResource.class);
        Deployment deployment = Mockito.mock(Deployment.class);
        MixedOperation<Service, ServiceList, ServiceResource<Service>> mixedOperationService = Mockito.mock(MixedOperation.class);
        ServiceResource serviceResource = Mockito.mock(ServiceResource.class);
        Service service = Mockito.mock(Service.class);
        Route route = Mockito.mock(Route.class);
        try (OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class)) {
            when(openShiftClient.projectrequests()).thenReturn(projectRequestOperation);
            when(openShiftClient.projectrequests().create(any())).thenReturn(projectRequest);
            when(openShiftClient.apps()).thenReturn(appsAPIGroupDSL);
            when(openShiftClient.apps().deployments()).thenReturn(mixedOperationDeployment);
            when(openShiftClient.apps().deployments().inNamespace(anyString())).thenReturn(nonNamespaceOperation);
            when(openShiftClient.apps().deployments().inNamespace(anyString()).resource(any())).thenReturn(rollableScalableResource);
            when(openShiftClient.apps().deployments().inNamespace(anyString()).resource(any(Deployment.class)).create()).thenReturn(deployment);
            when(openShiftClient.services()).thenReturn(mixedOperationService);
            when(openShiftClient.services().inNamespace(anyString())).thenReturn(nonNamespaceOperation);
            when(openShiftClient.services().inNamespace(anyString()).resource(any(Service.class))).thenReturn(serviceResource);
            when(openShiftClient.services().inNamespace(anyString()).resource(any(Service.class)).create()).thenReturn(service);
            when(openShiftClient.routes().inNamespace(anyString()).resource(any(Route.class)).create()).thenReturn(route);

            // when
            WorkReport workReport =   ocpAppDeploymentWorkFlowTask.execute(workContext);

            // then
            assertEquals(WorkStatus.COMPLETED, workReport.getStatus());

        }
    }

    @Test
    public void executeFail() {
        WorkContext workContext = Mockito.mock(WorkContext.class);
        try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
            restUtilsMockedStatic.when(() -> RestUtils.getRequestWithHeaders(any(NotificationRequest.class),
                            any(String.class), any(String.class)))
                    .thenReturn(new HttpEntity<>(NotificationRequest.builder().build()));

            restUtilsMockedStatic.when(() -> RestUtils.executePost(any(String.class), any(HttpEntity.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

            // when
            WorkReport workReport = ocpAppDeploymentWorkFlowTask.execute(workContext);

            // then
            assertEquals(WorkStatus.FAILED, workReport.getStatus());
        }
    }
}
