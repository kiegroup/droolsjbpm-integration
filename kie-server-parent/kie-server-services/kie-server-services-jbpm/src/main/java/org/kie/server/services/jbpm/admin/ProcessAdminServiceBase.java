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

package org.kie.server.services.jbpm.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.admin.MigrationEntry;
import org.jbpm.services.api.admin.MigrationReport;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.jbpm.services.api.admin.ProcessNode;
import org.jbpm.services.api.admin.TimerInstance;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.api.model.admin.ProcessNodeList;
import org.kie.server.api.model.admin.TimerInstanceList;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.jbpm.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessAdminServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAdminServiceBase.class);

    private ProcessInstanceMigrationService processInstanceMigrationService;
    private ProcessInstanceAdminService processInstanceAdminService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;

    public ProcessAdminServiceBase(ProcessInstanceMigrationService processInstanceMigrationService, ProcessInstanceAdminService processInstanceAdminService, KieServerRegistry context) {
        this.processInstanceMigrationService = processInstanceMigrationService;
        this.processInstanceAdminService = processInstanceAdminService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.context = context;
    }

    public MigrationReportInstance migrateProcessInstance(String containerId, Number processInstanceId, String targetContainerId, String targetProcessId, String payload, String marshallingType) {
        Map<String, String> nodeMapping = Collections.emptyMap();
        if (payload != null) {
            logger.debug("About to unmarshal node mapping from payload: '{}' using container {} marshaller", payload, containerId);
            nodeMapping = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);
        }
        logger.debug("About to migrate process instance with id {} from container '{}' to container '{}' and process id '{}' with node mapping {}",
                processInstanceId, containerId, targetContainerId, targetProcessId, nodeMapping);
        MigrationReport report = processInstanceMigrationService.migrate(containerId, processInstanceId.longValue(), targetContainerId, targetProcessId, nodeMapping);
        logger.debug("Migration of process instance {} finished with report {}", processInstanceId, report);
        return convertMigrationReport(report);
    }

    public MigrationReportInstanceList migrateProcessInstances(String containerId, List<Long> processInstancesId, String targetContainerId, String targetProcessId, String payload, String marshallingType) {
        Map<String, String> nodeMapping = Collections.emptyMap();
        if (payload != null) {
            logger.debug("About to unmarshal node mapping from payload: '{}' using container {} marshaller", payload, containerId);
            nodeMapping = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);
        }

        logger.debug("About to migrate process instances with ids {} from container '{}' to container '{}' and process id '{}' with node mapping {}",
                processInstancesId, containerId, targetContainerId, targetProcessId, nodeMapping);
        List<MigrationReport> reports = processInstanceMigrationService.migrate(containerId, convert(processInstancesId), targetContainerId, targetProcessId, nodeMapping);

        logger.debug("Migration of process instances {} finished with reports {}", processInstancesId, reports);
        return convertMigrationReports(reports);
    }


    public ProcessNodeList getProcessNodes(String containerId, Number processInstanceId) {
        logger.debug("About to get process nodes for process instance {} in container {}", processInstanceId, containerId);

        Collection<ProcessNode> processNodes = processInstanceAdminService.getProcessNodes(processInstanceId.longValue());
        logger.debug("Found process nodes {} for process instance {}", processNodes, processInstanceId);

        ProcessNodeList processNodeList = convertProcessNodes(processNodes);
        return processNodeList;
    }

    public void cancelNodeInstance(String containerId, Number processInstanceId, Number nodeInstanceId) {

        logger.debug("About to cancel node instance {} in process instance {}", nodeInstanceId, processInstanceId);

        processInstanceAdminService.cancelNodeInstance(processInstanceId.longValue(), nodeInstanceId.longValue());
        logger.debug("Node instance {} canceled successfully", nodeInstanceId);
    }

    public void retriggerNodeInstance(String containerId, Number processInstanceId, Number nodeInstanceId) {

        logger.debug("About to retrigger node instance {} in process instance {}", nodeInstanceId, processInstanceId);

        processInstanceAdminService.retriggerNodeInstance(processInstanceId.longValue(), nodeInstanceId.longValue());
        logger.debug("Node instance {} retriggered successfully", nodeInstanceId);
    }

    public NodeInstanceList getActiveNodeInstances(String containerId, Number processInstanceId) {
        logger.debug("About to get active node instance for process instance {} in container {}", processInstanceId, containerId);
        Collection<NodeInstanceDesc> activeNodeInstances = processInstanceAdminService.getActiveNodeInstances(processInstanceId.longValue());
        logger.debug("Found active node instance {} in process instance {}", activeNodeInstances, processInstanceId);

        return ConvertUtils.convertToNodeInstanceList(activeNodeInstances);
    }

    public void triggerNode(String containerId, Number processInstanceId, Number nodeId) {

        logger.debug("About to trigger (create) node {} in process instance {}", nodeId, processInstanceId);

        processInstanceAdminService.triggerNode(processInstanceId.longValue(), nodeId.longValue());
        logger.debug("Node {} triggered successfully", nodeId);
    }

    public void updateTimer(String containerId, Number processInstanceId, Number timerId, boolean relative, String payload, String marshallingType) {

        logger.debug("About to unmarshal timer update details from payload: '{}' using container {} marshaller", payload, containerId);
        Map<String, Number> timerUpdates = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);

        Number delay = timerUpdates.getOrDefault("delay", 0);
        Number period = timerUpdates.getOrDefault("period", 0);
        Number repeatLimit = timerUpdates.getOrDefault("repeatLimit", 0);

        if (relative) {

            logger.debug("Timer {} in process instance is going to be updated relatively to current time with values: delay {}, period {}, repeat limit {}",
                    timerId, processInstanceId, delay, period, repeatLimit);
            processInstanceAdminService.updateTimerRelative(processInstanceId.longValue(), timerId.longValue(), delay.longValue(), period.longValue(), repeatLimit.intValue());
        } else {
            logger.debug("Timer {} in process instance is going to be updated with values: delay {}, period {}, repeat limit {}",
                    timerId, processInstanceId, delay, period, repeatLimit);
            processInstanceAdminService.updateTimer(processInstanceId.longValue(), timerId.longValue(), delay.longValue(), period.longValue(), repeatLimit.intValue());
        }
        logger.debug("Timer {} triggered successfully in process instance", timerId, processInstanceId);
    }

    public TimerInstanceList getTimerInstances(String containerId, Number processInstanceId) {
        logger.debug("About to get timers for process instance {} in container {}", processInstanceId, containerId);
        Collection<TimerInstance> timerInstances = processInstanceAdminService.getTimerInstances(processInstanceId.longValue());

        logger.debug("Found timers {} in process instance {}", timerInstances, processInstanceId);
        TimerInstanceList timerInstanceList = convertTimerInstances(timerInstances);

        return timerInstanceList;
    }

    /*
     * helper methods
     */

    protected MigrationReportInstanceList convertMigrationReports(List<MigrationReport> reports) {

        if (reports == null) {
            return new MigrationReportInstanceList();
        }
        MigrationReportInstance[] reportInstances = new MigrationReportInstance[reports.size()];
        int index = 0;
        for (MigrationReport report : reports) {

            MigrationReportInstance instance = convertMigrationReport(report);
            reportInstances[index] = instance;

            index++;
        }
        return new MigrationReportInstanceList(reportInstances);
    }

    protected MigrationReportInstance convertMigrationReport(MigrationReport report) {
        if (report == null) {
            return null;
        }
        MigrationReportInstance reportInstance = MigrationReportInstance.builder()
                .successful(report.isSuccessful())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .logs(convertLogs(report.getEntries()))
                .build();

        return reportInstance;
    }

    protected ProcessNodeList convertProcessNodes(Collection<ProcessNode> processNodes) {

        if (processNodes == null) {
            return new ProcessNodeList();
        }
        org.kie.server.api.model.admin.ProcessNode[] processNodesConverted = new org.kie.server.api.model.admin.ProcessNode[processNodes.size()];
        int index = 0;
        for (ProcessNode processNode : processNodes) {

            org.kie.server.api.model.admin.ProcessNode instance = convertProcessNode(processNode);
            processNodesConverted[index] = instance;

            index++;
        }
        return new ProcessNodeList(processNodesConverted);
    }

    protected org.kie.server.api.model.admin.ProcessNode convertProcessNode(ProcessNode processNode) {
        if (processNode == null) {
            return null;
        }
        org.kie.server.api.model.admin.ProcessNode processNodeConverted = org.kie.server.api.model.admin.ProcessNode.builder()
                .nodeId(processNode.getNodeId())
                .nodeName(processNode.getNodeName())
                .nodeType(processNode.getNodeType())
                .processId(processNode.getProcessId())
                .build();

        return processNodeConverted;
    }

    protected TimerInstanceList convertTimerInstances(Collection<TimerInstance> timerInstances) {

        if (timerInstances == null) {
            return new TimerInstanceList();
        }
        org.kie.server.api.model.admin.TimerInstance[] timerInstancesConverted = new org.kie.server.api.model.admin.TimerInstance[timerInstances.size()];
        int index = 0;
        for (TimerInstance timerInstance : timerInstances) {

            org.kie.server.api.model.admin.TimerInstance instance = convertTimerInstance(timerInstance);
            timerInstancesConverted[index] = instance;

            index++;
        }
        return new TimerInstanceList(timerInstancesConverted);
    }

    protected org.kie.server.api.model.admin.TimerInstance convertTimerInstance(TimerInstance timerInstance) {
        if (timerInstance == null) {
            return null;
        }
        org.kie.server.api.model.admin.TimerInstance timerInstanceConverted = org.kie.server.api.model.admin.TimerInstance.builder()
                .activationTime(timerInstance.getActivationTime())
                .delay(timerInstance.getDelay())
                .lastFireTime(timerInstance.getLastFireTime())
                .nextFireTime(timerInstance.getNextFireTime())
                .period(timerInstance.getPeriod())
                .processInstanceId(timerInstance.getProcessInstanceId())
                .repeatLimit(timerInstance.getRepeatLimit())
                .sessionId(timerInstance.getSessionId())
                .timerId(timerInstance.getTimerId())
                .timerName(timerInstance.getTimerName())
                .build();

        return timerInstanceConverted;
    }

    protected List<String> convertLogs(List<MigrationEntry> entries) {

        List<String> logs = new ArrayList<String>();
        if (entries != null) {
            for (MigrationEntry entry : entries) {
                logs.add(entry.getType() + " " + entry.getTimestamp() + " " + entry.getMessage());
            }
        }
        return logs;
    }

    protected List<Long> convert(List<? extends Number> input) {
        List<Long> result = new ArrayList<Long>();

        for (Number n : input) {
            result.add(n.longValue());
        }

        return result;
    }
}
