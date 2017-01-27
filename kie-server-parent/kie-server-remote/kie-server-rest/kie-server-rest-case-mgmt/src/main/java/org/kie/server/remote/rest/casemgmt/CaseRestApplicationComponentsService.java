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

package org.kie.server.remote.rest.casemgmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.casemgmt.CaseKieServerExtension;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.casemgmt.CaseManagementServiceBase;

public class CaseRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = CaseKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        // skip calls from other than owning extension
        if ( !OWNER_EXTENSION.equals(extension) ) {
            return Collections.emptyList();
        }
        CaseManagementServiceBase caseManagementServiceBase = null;
        CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase = null;
        KieServerRegistry context = null;

        for( Object object : services ) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object == null) {
                continue;
            }
            if( CaseManagementServiceBase.class.isAssignableFrom(object.getClass()) ) {
                caseManagementServiceBase = (CaseManagementServiceBase) object;
                continue;
            } else if( CaseManagementRuntimeDataServiceBase.class.isAssignableFrom(object.getClass()) ) {
                caseManagementRuntimeDataServiceBase = (CaseManagementRuntimeDataServiceBase) object;
                continue;
            } else if( KieServerRegistry.class.isAssignableFrom(object.getClass()) ) {
                context = (KieServerRegistry) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>();

        components.add(new CaseResource(caseManagementServiceBase, caseManagementRuntimeDataServiceBase, context));
        components.add(new CaseQueryResource(caseManagementRuntimeDataServiceBase, context));
        components.add(new CaseAdminResource(caseManagementRuntimeDataServiceBase, context));

        return components;
    }
}
