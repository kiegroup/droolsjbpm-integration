/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.casemgmt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.CaseService;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.services.api.KieServerRegistry;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseManagementServiceBaseTest {

    @Mock
    CaseService caseService;

    @Mock
    CaseRuntimeDataService caseRuntimeDataService;

    @Mock
    KieServerRegistry context;

    @Mock
    IdentityProvider identityProvider;

    CaseManagementServiceBase service;

    @Before
    public void init() {
        when(context.getIdentityProvider()).thenReturn(identityProvider);
        when(context.getConfig()).thenReturn(new KieServerConfig());

        service = spy(new CaseManagementServiceBase(caseService,
                                                    caseRuntimeDataService,
                                                    context));
    }

    @Test
    public void testMultipleRolesAssignments() {
        CaseFile caseFile = CaseFile.builder().addUserAssignments("participant",
                                                                  "user1",
                                                                  "user2").addGroupAssignments("participant",
                                                                                               "group1",
                                                                                               "group2").build();

        final Map<String, OrganizationalEntity[]> rolesAssignments = service.getRolesAssignments(caseFile);
        assertNotNull(rolesAssignments);
        assertEquals(1,
                     rolesAssignments.keySet().size());
        final OrganizationalEntity[] participants = rolesAssignments.get("participant");
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof UserImpl && "user1".equals(oe.getId())).findAny().isPresent());
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof UserImpl && "user2".equals(oe.getId())).findAny().isPresent());
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof GroupImpl && "group1".equals(oe.getId())).findAny().isPresent());
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof GroupImpl && "group2".equals(oe.getId())).findAny().isPresent());
    }

    @Test
    public void testMultipleRolesAssignmentsUsingMap() {
        Map<String, String[]> users = new HashMap<>();
        users.put("participant",
                  new String[]{"user1", "user2"});
        Map<String, String[]> groups = new HashMap<>();
        groups.put("participant",
                   new String[]{"group1", "group2"});
        CaseFile caseFile = CaseFile.builder().userAssignments(users).groupAssignments(groups).build();

        final Map<String, OrganizationalEntity[]> rolesAssignments = service.getRolesAssignments(caseFile);
        assertNotNull(rolesAssignments);
        assertEquals(1,
                     rolesAssignments.keySet().size());
        final OrganizationalEntity[] participants = rolesAssignments.get("participant");
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof UserImpl && "user1".equals(oe.getId())).findAny().isPresent());
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof UserImpl && "user2".equals(oe.getId())).findAny().isPresent());
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof GroupImpl && "group1".equals(oe.getId())).findAny().isPresent());
        assertTrue(Arrays.stream(participants).filter(oe -> oe instanceof GroupImpl && "group2".equals(oe.getId())).findAny().isPresent());
    }
}
