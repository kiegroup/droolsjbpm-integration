/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client.admin;

import java.util.List;
import java.util.Map;

import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.ProcessNode;
import org.kie.server.api.model.admin.TimerInstance;
import org.kie.server.api.model.instance.NodeInstance;

public interface ProcessAdminServicesClient {

    MigrationReportInstance migrateProcessInstance(String containerId, Long processInstanceId, String targetContainerId, String targetProcessId);

    MigrationReportInstance migrateProcessInstance(String containerId, Long processInstanceId, String targetContainerId, String targetProcessId, Map<String, String> nodeMapping);

    List<MigrationReportInstance> migrateProcessInstances(String containerId, List<Long> processInstancesId, String targetContainerId, String targetProcessId);

    List<MigrationReportInstance> migrateProcessInstances(String containerId, List<Long> processInstancesId, String targetContainerId, String targetProcessId, Map<String, String> nodeMapping);

    List<ProcessNode> getProcessNodes(String containerId, Long processInstanceId);

    void cancelNodeInstance(String containerId, Long processInstanceId, Long nodeInstanceId);

    void retriggerNodeInstance(String containerId, Long processInstanceId, Long nodeInstanceId);

    List<NodeInstance> getActiveNodeInstances(String containerId, Long processInstanceId);

    void updateTimer(String containerId, Long processInstanceId, long timerId, long delay, long period, int repeatLimit);

    void updateTimerRelative(String containerId, Long processInstanceId, long timerId, long delay, long period, int repeatLimit);

    List<TimerInstance> getTimerInstances(String containerId, Long processInstanceId);

    void triggerNode(String containerId, Long processInstanceId, Long nodeId);
}
