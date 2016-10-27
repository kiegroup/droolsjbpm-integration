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

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.kie.api.command.Command;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.kie.server.client.UserTaskServicesClient;

public class KieServerSynchronization {

    private static final long SERVICE_TIMEOUT = 30000;
    private static final long TIMEOUT_BETWEEN_CALLS = 200;

    public static void waitForJobToFinish(final JobServicesClient jobServicesClient, final Long jobId) throws Exception {
        waitForCondition(() -> {
            RequestInfoInstance result = jobServicesClient.getRequestById(jobId, false, false);

            // If job finished (to one of final states) then return.
            if (STATUS.CANCELLED.toString().equals(result.getStatus()) ||
                    STATUS.DONE.toString().equals(result.getStatus()) ||
                    STATUS.ERROR.toString().equals(result.getStatus())) {
                return true;
            }
            return false;
        });
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
        waitForCondition(() -> {
            List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);

            if (processInstances.size() == 1) {
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
            ServiceResponse<SolverInstanceList> solverListResponse = client.getSolvers(containerId);
            SolverInstanceList solverList = solverListResponse.getResult();
            for (SolverInstance solver : solverList.getContainers()) {
                if (solverId.equals(solver.getSolverId())) {
                    return true;
                }
            }
            return false;
        });
    }

    public static void waitForSolverDispose(final SolverServicesClient client, final String containerId, final String solverId) throws Exception {
        waitForCondition(() -> {
            ServiceResponse<SolverInstanceList> solverListResponse = client.getSolvers(containerId);
            SolverInstanceList solverList = solverListResponse.getResult();
            for (SolverInstance solver : solverList.getContainers()) {
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
            ServiceResponse<SolverInstance> solverResponse = client.getSolverState(containerId, solverId);
            return status.equals(solverResponse.getResult().getStatus());
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
        waitForCondition(() -> !client.getQueries(0, 10).stream()
                .filter(q -> query.getName().equals(q.getName()) && query.getExpression().equals(q.getExpression()))
                .collect(Collectors.toList())
                .isEmpty()
        );
    }

    public static void waitForQueryRemoval(final QueryServicesClient client, final QueryDefinition query) throws Exception {
        waitForCondition(() -> client.getQueries(0, 10).stream()
                .filter(q -> query.getName().equals(q.getName()))
                .collect(Collectors.toList())
                .isEmpty()
        );
    }

    /**
     * @param condition Condition result supplier. If returns true then condition is met.
     * @throws Exception
     */
    private static void waitForCondition(BooleanSupplier condition) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {

            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(TIMEOUT_BETWEEN_CALLS);
        }
        throw new TimeoutException("Synchronization failed for defined timeout: " + SERVICE_TIMEOUT + " milliseconds.");
    }
}
