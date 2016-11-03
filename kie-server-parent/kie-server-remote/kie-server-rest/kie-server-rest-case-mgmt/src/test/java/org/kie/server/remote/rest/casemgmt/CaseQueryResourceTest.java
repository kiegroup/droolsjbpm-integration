/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.remote.rest.casemgmt;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseQueryResourceTest {

    @Mock
    HttpHeaders httpHeaders;

    @Mock
    CaseManagementRuntimeDataServiceBase runtimeDataService;

    @Spy
    KieServerRegistry kieServerRegistry = new KieServerRegistryImpl();

    CaseQueryResource caseQueryResource;

    @Before
    public void init() {
        when(httpHeaders.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());

        caseQueryResource = new CaseQueryResource(runtimeDataService, kieServerRegistry);
    }

    @Test
    public void testGetCaseDefinitions(){
        String filter = null;
        Integer page = 0;
        Integer pageSize= 10;
        String sort = "CaseId";
        boolean sortOrder = true;
        when(runtimeDataService.getCaseDefinitions(filter, page, pageSize, sort, sortOrder)).thenReturn(new CaseDefinitionList());

        caseQueryResource.getCaseDefinitions(httpHeaders, filter, page, pageSize, sort, sortOrder);

        verify(kieServerRegistry).getContainer("");
        verify(runtimeDataService).getCaseDefinitions(filter, page, pageSize, sort, sortOrder);
    }

    @Test
    public void testGetCaseInstances(){
        List<Integer> status = null;
        Integer page = 0;
        Integer pageSize= 10;
        String sort = "CorrelationKey";
        boolean sortOrder = true;
        when(runtimeDataService.getCaseInstances(status, page, pageSize, sort, sortOrder)).thenReturn(new CaseInstanceList());

        caseQueryResource.getCaseInstances(httpHeaders, null, status, page, pageSize, sort, sortOrder);

        verify(kieServerRegistry).getContainer("");
        verify(runtimeDataService).getCaseInstances(status, page, pageSize, sort, sortOrder);
    }

}