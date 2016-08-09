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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.admin.MigrationEntry;
import org.jbpm.services.api.admin.MigrationReport;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessAdminServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAdminServiceBase.class);

    private ProcessInstanceMigrationService processInstanceMigrationService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;

    public ProcessAdminServiceBase(ProcessInstanceMigrationService processInstanceMigrationService, KieServerRegistry context) {
        this.processInstanceMigrationService = processInstanceMigrationService;
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
