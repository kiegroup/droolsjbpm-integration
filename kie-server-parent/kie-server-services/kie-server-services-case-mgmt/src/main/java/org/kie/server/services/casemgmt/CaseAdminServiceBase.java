/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.casemgmt;

import java.util.Collections;
import java.util.Map;

import org.jbpm.casemgmt.api.admin.CaseInstanceMigrationService;
import org.jbpm.casemgmt.api.admin.CaseMigrationReport;
import org.kie.server.api.model.cases.CaseMigrationReportInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseAdminServiceBase {
    
    private static final Logger logger = LoggerFactory.getLogger(CaseAdminServiceBase.class);

    private CaseInstanceMigrationService caseInstanceMigrationService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;
    
    
    public CaseAdminServiceBase(CaseInstanceMigrationService caseInstanceMigrationService, KieServerRegistry context) {
        this.caseInstanceMigrationService = caseInstanceMigrationService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.context = context;
    }
    
    public CaseMigrationReportInstance migrateCaseInstance(String containerId, String caseId, String targetContainerId, String payload, String marshallingType) {
        Map<String, Object> mapping = Collections.emptyMap();
        if (payload != null) {
            logger.debug("About to unmarshal mapping from payload: '{}' using container {} marshaller", payload, containerId);
            mapping = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);
        }
        if (mapping == null || !mapping.containsKey("ProcessMapping")) {
            throw new IllegalArgumentException("Case instance migration requires ProcessMapping to be provided");
        }
        Map<String, String> processMapping = (Map<String, String>) mapping.get("ProcessMapping");
        Map<String, String> nodeMapping = (Map<String, String>) mapping.getOrDefault("NodeMapping", Collections.emptyMap());
        
        logger.debug("About to migrate case instance with id {} from container '{}' to container '{}' with process mapping '{}' and with node mapping {}",
                     caseId, containerId, targetContainerId, processMapping, nodeMapping);
        
        CaseMigrationReport report = caseInstanceMigrationService.migrate(caseId, targetContainerId, processMapping, nodeMapping);
        logger.debug("Migration of case instance {} finished with report {}", caseId, report);
        return ConvertUtils.convertCaseMigrationReport(caseId, report);
    }
}
