/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.shared;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import org.kie.api.command.Command;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.Message;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;

public class KieServerSynchronization {

    private static final long SERVICE_TIMEOUT = 60000;
    private static final long TIMEOUT_BETWEEN_CALLS = 400;

    public static void waitForJobToFinish(final JobServicesClient jobServicesClient, final Long jobId) throws Exception {
        waitForJobToFinish(jobServicesClient, jobId, SERVICE_TIMEOUT);
    }

    public static void waitForJobToFinish(final JobServicesClient jobServicesClient, final Long jobId, final Long timeOut) throws Exception {
        waitForCondition(() -> {
            RequestInfoInstance result = jobServicesClient.getRequestById(jobId, false, false);

            // If job finished (to one of final states) then return.
            if (STATUS.CANCELLED.toString().equals(result.getStatus()) ||
                    STATUS.DONE.toString().equals(result.getStatus()) ||
                    STATUS.ERROR.toString().equals(result.getStatus())) {
                return true;
            }
            return false;
        }, timeOut);
    }

    public static void waitForKieServerSynchronization(final KieServicesClient client, final int numberOfExpectedContainers) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<KieContainerResourceList> containersList = client.listContainers();

            // If synchronization finished (number of containers same as expected) then return.
            if (containersList.getResult().getContainers() == null) {
                if (numberOfExpectedContainers == 0) {
                    return true;
                }
            } else if (numberOfExpectedContainers == containersList.getResult().getContainers().size()) {
                // Check that all containers are created or disposed.
                boolean containersInitializing = false;
                for (KieContainerResource container : containersList.getResult().getContainers()) {
                    if (KieContainerStatus.CREATING.equals(container.getStatus()) ||
                            KieContainerStatus.DISPOSING.equals(container.getStatus())) {
                        containersInitializing = true;
                    }
                }
                if (!containersInitializing) {
                    return true;
                }
            }
            return false;
        });
    }

    public static void waitForKieServerMessage(final KieServicesClient client, final String containerId, final String expectedMessage) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<KieContainerResource> containerResponse = client.getContainerInfo(containerId);
            if (!containerResponse.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                return false;
            }

            // Kie Container store only one message
            List<Message> messagesList = containerResponse.getResult().getMessages();
            if (messagesList.size() == 1) {
                return messagesList.get(0).getMessages().stream().anyMatch(n -> n.equals(expectedMessage));
            }

            return false;
        });
    }

    public static void waitForProcessInstanceToFinish(final ProcessServicesClient processClient, final String containerId, final long processInstanceId) throws Exception {
        waitForCondition(() -> {
            ProcessInstance processInstance = processClient.getProcessInstance(containerId, processInstanceId);

            // If process instance is finished (to one of final states) then return.
            if (((Integer) org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED).equals(processInstance.getState()) || ((Integer) org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED).equals(processInstance.getState())) {
                return true;
            }
            return false;
        });
    }

    public static void waitForProcessInstanceStart(final QueryServicesClient queryClient, final String containerId) throws Exception {
        waitForProcessInstanceStart(queryClient, containerId, 1, Arrays.asList(1));
    }

    public static void waitForProcessInstanceStart(final QueryServicesClient queryClient, final String containerId, int expectedInstances, List<Integer> statuses) throws Exception {
        waitForCondition(() -> {
            List<ProcessInstance> processInstances = queryClient.findProcessInstancesByContainerId(containerId, statuses, 0, 100);

            if (processInstances.size() == expectedInstances) {
                return true;
            }
            return false;
        });
    }

    public static void waitForContainerWithReleaseId(final KieServicesClient client, final ReleaseId releaseId) throws Exception {
        waitForCondition(() -> {
            ReleaseIdFilter releaseIdFilter = new ReleaseIdFilter(releaseId);
            KieContainerResourceFilter resourceFilter = new KieContainerResourceFilter(releaseIdFilter);

            ServiceResponse<KieContainerResourceList> containersList = client.listContainers(resourceFilter);
            List<KieContainerResource> containers = containersList.getResult().getContainers();
            return containers != null && !containers.isEmpty();
        });
    }

    public static void waitForContainerWithScannerStatus(final KieServicesClient client, final KieScannerStatus scannerStatus) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<KieContainerResourceList> containersList = client.listContainers();
            List<KieContainerResource> containers = containersList.getResult().getContainers();
            if (containers != null) {
                for (KieContainerResource container : containers) {
                    KieScannerResource scanner = container.getScanner();
                    if (scanner != null && scannerStatus.equals(scanner.getStatus())) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public static void waitForSolver(final SolverServicesClient client, final String containerId, final String solverId) throws Exception {
        waitForCondition(() -> {
            List<SolverInstance> solverInstanceList = client.getSolvers(containerId);
            for (SolverInstance solver : solverInstanceList) {
                if (solverId.equals(solver.getSolverId())) {
                    return true;
                }
            }
            return false;
        });
    }

    public static void waitForSolverDispose(final SolverServicesClient client, final String containerId, final String solverId) throws Exception {
        waitForCondition(() -> {
            List<SolverInstance> solverInstanceList = client.getSolvers(containerId);
            for (SolverInstance solver : solverInstanceList) {
                if (solverId.equals(solver.getSolverId())) {
                    return false;
                }
            }
            return true;
        });
    }

    public static void waitForSolverStatus(final SolverServicesClient client, final String containerId, final String solverId,
            final SolverInstance.SolverStatus status) throws Exception {
        waitForCondition(() -> {
            SolverInstance solverInstance = client.getSolver(containerId,
                                                             solverId);
            return status.equals(solverInstance.getStatus());
        });
    }

    public static void waitForCommandResult(final RuleServicesClient client, final String containerId,
            final Command command, final String identifier, final Object value) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<ExecutionResults> response = client.executeCommandsWithResults(containerId, command);
            ExecutionResults result = response.getResult();
            return value.equals(result.getValue(identifier));
        });
    }

    public static void waitForTaskStatus(final UserTaskServicesClient client, final Long taskId, final String status) throws Exception {
        waitForCondition(() -> {
            TaskInstance task = client.findTaskById(taskId);
            return status.equals(task.getStatus());
        });
    }

    public static void waitForQuery(final QueryServicesClient client, final QueryDefinition query) throws Exception {
        waitForCondition(() -> {
            try {
                QueryDefinition q = client.getQuery(query.getName());
                return query.getExpression().equals(q.getExpression());
            } catch (KieServicesException e) {
                // Query isn't created yet
                return false;
            }
        });
    }

    public static void waitForQueryRemoval(final QueryServicesClient client, final QueryDefinition query) throws Exception {
        waitForCondition(() -> {
            try {
                client.getQuery(query.getName());
                return false;
            } catch (KieServicesException e) {
                // Query doesn't exist any more
                return true;
            }
        });
    }

    public static void waitForServerInstanceSynchronization(final KieServerControllerClient controllerClient, String serverTemplateId, final int numberOfExpectedServerInstances) throws Exception {
        waitForCondition(() -> {
            ServerTemplate serverTemplate = controllerClient.getServerTemplate(serverTemplateId);
            if (serverTemplate != null && serverTemplate.getServerInstanceKeys().size() == numberOfExpectedServerInstances) {
                return true;
            }
            return false;
        });
    }

    public static void waitForServerTemplateSynchronization(final KieServerControllerClient controllerClient, final int numberOfExpectedServerTemplates) throws Exception {
        waitForCondition(() -> {
            ServerTemplate[] serverTemplates = controllerClient.listServerTemplates().getServerTemplates();
            int numberOfServerTemplates = serverTemplates == null ? 0 : serverTemplates.length;
            return numberOfServerTemplates == numberOfExpectedServerTemplates;
        });
    }

    public static void waitForKieServerScannerStatus(final KieServicesClient client, final String containerId, final KieScannerStatus scannerStatus) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<KieContainerResource> containerResponse = client.getContainerInfo(containerId);
            if (!containerResponse.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                return false;
            }

            KieScannerResource scanner = containerResponse.getResult().getScanner();
            if (scanner.getStatus().equals(scannerStatus)) {
                return true;
            }

            return false;
        });
    }

    public static void waitForKieServerScannerStatus(final KieServicesClient client, final String containerId, final KieScannerStatus scannerStatus, final Long pollInterval) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<KieContainerResource> containerResponse = client.getContainerInfo(containerId);
            if (!containerResponse.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                return false;
            }

            KieScannerResource scanner = containerResponse.getResult().getScanner();
            if (scanner.getStatus().equals(scannerStatus) && scanner.getPollInterval().equals(pollInterval)) {
                return true;
            }

            return false;
        });
    }

    public static void waitForKieServerConfig(final KieServicesClient client, final String containerId, final String configItemName, final String configItemValue) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<KieContainerResource> containerResponse = client.getContainerInfo(containerId);
            if (!containerResponse.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                return false;
            }

            List<KieServerConfigItem> configItems = containerResponse.getResult().getConfigItems();
            for (KieServerConfigItem configItem : configItems) {
                if (configItem.getName().equals(configItemName) && configItem.getValue().equals(configItemValue)) {
                    return true;
                }
            }

            return false;
        });
    }

    public static void waitForProcessInstanceSLAViolated(final QueryServicesClient queryClient, final Long processInstanceId) throws Exception {
        waitForProcessInstanceSLAViolated(queryClient, processInstanceId, SERVICE_TIMEOUT);
    }

    public static void waitForProcessInstanceSLAViolated(final QueryServicesClient queryClient, final Long processInstanceId, final Long timeOut) throws Exception {
        waitForCondition(() -> {
            ProcessInstance pi = queryClient.findProcessInstanceById(processInstanceId);
            return pi.getSlaCompliance() == org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED;
        }, timeOut);
    }

    public static void waitForNodeInstanceSLAViolated(final QueryServicesClient queryClient, final Long processInstanceId, final Long workItemId) throws Exception {
        waitForNodeInstanceSLAViolated(queryClient, processInstanceId, workItemId, SERVICE_TIMEOUT);
    }

    public static void waitForNodeInstanceSLAViolated(final QueryServicesClient queryClient, final Long processInstanceId, final Long nodeId, final Long timeOut) throws Exception {
        waitForCondition(() -> {
            List<NodeInstance> nodes = queryClient.findActiveNodeInstances(processInstanceId, 0, 0);
            Optional<NodeInstance> ni = nodes.stream()
                    .filter(nInstance -> nInstance.getId().equals(nodeId))
                    .findFirst();
            return ni.isPresent() && (ni.get().getSlaCompliance() == org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED);
        }, timeOut);
    }

    /**
     * @param condition Condition result supplier. If returns true then condition is met.
     * @throws Exception
     */
    public static void waitForCondition(BooleanSupplier condition) throws Exception {
        waitForCondition(condition, SERVICE_TIMEOUT);
    }

    /**
     * @param condition Condition result supplier. If returns true then condition is met.
     * @throws Exception
     */
    public static void waitForCondition(BooleanSupplier condition, long timeOut) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + timeOut;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {

            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(TIMEOUT_BETWEEN_CALLS);
        }
        throw new TimeoutException("Synchronization failed for defined timeout: " + timeOut + " milliseconds.");
    }
}
