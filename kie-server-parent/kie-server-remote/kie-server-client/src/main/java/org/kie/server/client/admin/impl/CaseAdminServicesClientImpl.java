/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.admin.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseMigrationReportInstance;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.admin.CaseAdminServicesClient;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;

import static org.kie.server.api.rest.RestURI.*;

public class CaseAdminServicesClientImpl extends AbstractKieServicesClientImpl implements CaseAdminServicesClient {

    public CaseAdminServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public CaseAdminServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public List<CaseInstance> getCaseInstances(Integer page, Integer pageSize) {
        return getCaseInstances(null, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstances(List<String> status, Integer page, Integer pageSize) {
        return getCaseInstances(status, page, pageSize, "", true);
    }

    @Override
    public List<CaseInstance> getCaseInstances(Integer page, Integer pageSize, String sort, boolean sortOrder) {
        return getCaseInstances(null, page, pageSize, sort, sortOrder);
    }

    @Override
    public List<CaseInstance> getCaseInstances(List<String> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        CaseInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);
            queryString = getAdditionalParams(queryString, "status", status);
            queryString = getSortingQueryString(queryString, sort, sortOrder);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_CASE_URI + "/" + ADMIN_CASE_ALL_INSTANCES_GET_URI, valuesMap) + queryString, CaseInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseQueryService", "getCaseInstances", new Object[]{safeList(status), page, pageSize, sort, sortOrder})) );
            ServiceResponse<CaseInstanceList> response = (ServiceResponse<CaseInstanceList>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            list = response.getResult();
        }

        if (list != null) {
            return list.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public CaseMigrationReportInstance migrateCaseInstance(String containerId, String caseId, String targetContainerId, Map<String, String> processMapping) {
        
        return migrateCaseInstance(containerId, caseId, targetContainerId, processMapping, new HashMap<>());
    }

    @Override
    public CaseMigrationReportInstance migrateCaseInstance(String containerId, String caseId, String targetContainerId, Map<String, String> processMapping, Map<String, String> nodeMapping) {
        CaseMigrationReportInstance report = null;
        
        Map<String, Object> mappings = new HashMap<>();
        mappings.put("ProcessMapping", processMapping);
        mappings.put("NodeMapping", nodeMapping);
        
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(CASE_ID, caseId);
            
            String queryString = "?targetContainerId=" + targetContainerId;

            report = makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_CASE_URI + "/" + MIGRATE_CASE_INST_PUT_URI, valuesMap) + queryString, serialize(mappings), CaseMigrationReportInstance.class, new HashMap<>());

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("CaseAdminService", "migrateCaseInstance",  serialize(safeMap(mappings)), marshaller.getFormat().getType(), new Object[]{containerId, caseId, targetContainerId})) );
            ServiceResponse<CaseMigrationReportInstance> response = (ServiceResponse<CaseMigrationReportInstance>)
                    executeJmsCommand( script, DescriptorCommand.class.getName(), KieServerConstants.CAPABILITY_CASE ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            report = response.getResult();
        }


        return report;


    }
}
