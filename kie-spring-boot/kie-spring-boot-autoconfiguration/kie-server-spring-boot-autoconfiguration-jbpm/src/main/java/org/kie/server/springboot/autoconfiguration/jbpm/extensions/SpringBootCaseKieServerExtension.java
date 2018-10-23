/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.autoconfiguration.jbpm.extensions;

import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.CaseService;
import org.jbpm.casemgmt.api.admin.CaseInstanceMigrationService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseAdminServiceBase;
import org.kie.server.services.casemgmt.CaseKieServerExtension;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.casemgmt.CaseManagementServiceBase;
import org.kie.server.services.impl.KieServerImpl;

public class SpringBootCaseKieServerExtension extends CaseKieServerExtension {

    private CaseService caseService;
    private CaseInstanceMigrationService caseInstanceMigrationService;
    
    public SpringBootCaseKieServerExtension(CaseService caseService,
                                            CaseRuntimeDataService caseRuntimeDataService,
                                            CaseInstanceMigrationService caseInstanceMigrationService) {
        this.caseService = caseService;
        this.caseRuntimeDataService = caseRuntimeDataService;
        this.caseInstanceMigrationService = caseInstanceMigrationService;
    }

    @Override
    protected void configureServices(KieServerImpl kieServer, KieServerRegistry registry) {
        this.caseManagementServiceBase = new CaseManagementServiceBase(caseService, caseRuntimeDataService, registry);
        this.caseManagementRuntimeDataService = new CaseManagementRuntimeDataServiceBase(caseRuntimeDataService, registry);
        this.caseAdminServiceBase = new CaseAdminServiceBase(caseInstanceMigrationService, registry);
        
    }

}
