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

package org.kie.processmigration.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kie.processmigration.model.Execution;
import org.kie.processmigration.model.Execution.ExecutionStatus;
import org.kie.processmigration.model.Execution.ExecutionType;
import org.kie.processmigration.model.Migration;
import org.kie.processmigration.model.MigrationDefinition;
import org.kie.processmigration.model.MigrationReport;
import org.kie.processmigration.model.Plan;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.processmigration.model.exceptions.InvalidMigrationException;
import org.kie.processmigration.model.exceptions.MigrationNotFoundException;
import org.kie.processmigration.model.exceptions.ProcessNotFoundException;
import org.kie.processmigration.model.exceptions.ReScheduleException;
import org.kie.processmigration.service.impl.MigrationServiceImpl;
import org.kie.processmigration.service.impl.PlanServiceImpl;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MigrationServiceImplTest extends AbstractPersistenceTest {

    private static final Execution SYNC = new Execution().setType(ExecutionType.SYNC);

    @Rule
    public WeldInitiator weld = WeldInitiator
            .from(PlanServiceImpl.class, MigrationServiceImpl.class, TransactionHelper.class)
            .addBeans(createMockBean(KieService.class), createMockBean(SchedulerService.class))
            .setPersistenceContextFactory(getPCFactory())
            .inject(this)
            .build();
    @Inject
    private MigrationService migrationService;
    @Inject
    private PlanService planService;
    @Inject
    private KieService kieServiceMock;
    @Inject
    private SchedulerService schedulerServiceMock;

    @Before
    public void resetMocks() {
        Mockito.reset(kieServiceMock, schedulerServiceMock);
    }

    @Test(expected = MigrationNotFoundException.class)
    public void testGetNotFound() throws MigrationNotFoundException {
        migrationService.get(8888L);
    }

    @Test
    public void testSubmitMigration() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        for (long i = 1; i <= 3; i++) {
            Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(i))).thenReturn(buildProcessInstance(i, plan.getSourceContainerId()));
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }

        Mockito.when(kieServiceMock.getQueryServicesClient(anyString())).thenReturn(mockQueryServicesClient);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        // Then
        for (long i = 1; i <= 3; i++) {
            verify(mockProcessAdminServicesClient).migrateProcessInstance(plan.getSourceContainerId(),
                                                                          i,
                                                                          plan.getTargetContainerId(),
                                                                          plan.getTargetProcessId(),
                                                                          plan.getMappings());
        }
        List<Migration> migrations = migrationService.findAll();

        assertNotNull(migrations);
        assertEquals(1, migrations.size());
        Migration m = migrations.get(0);
        assertEquals(ExecutionStatus.COMPLETED, m.getStatus());
        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getStartedAt());
        assertNotNull(m.getFinishedAt());
        List<MigrationReport> results = migrationService.getResults(m.getId());
        assertEquals(3, results.size());
        results.stream().forEach(r -> assertTrue(r.getSuccessful()));
    }

    @Test
    public void testSubmitAllMigrationNoProcessInstances() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);
        def.getProcessInstanceIds().clear();

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);

        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString())).thenReturn(mockQueryServicesClient);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        // Then
        List<Migration> migrations = migrationService.findAll();

        assertNotNull(migrations);
        assertEquals(1, migrations.size());
        Migration m = migrations.get(0);
        assertEquals(ExecutionStatus.COMPLETED, m.getStatus());
        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getStartedAt());
        assertNotNull(m.getFinishedAt());
        List<MigrationReport> results = migrationService.getResults(m.getId());
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSubmitAllProcessesMigration() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);
        def.getProcessInstanceIds().clear();

        // Setup mocks
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        for (long i = 1; i <= 110; i++) {
            Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(i))).thenReturn(buildProcessInstance(i, plan.getSourceContainerId()));
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString()))
                .thenReturn(mockProcessAdminServicesClient);
        Mockito.when(mockQueryServicesClient.findProcessInstancesByContainerId(anyString(), anyListOf(Integer.class), eq(0), anyInt()))
                .thenReturn(buildProcessInstances(1, 100));
        Mockito.when(mockQueryServicesClient.findProcessInstancesByContainerId(anyString(), anyListOf(Integer.class), eq(1), anyInt()))
                .thenReturn(buildProcessInstances(101, 110));
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString()))
                .thenReturn(mockQueryServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        // Then
        for (long i = 1; i <= 110; i++) {
            verify(mockProcessAdminServicesClient).migrateProcessInstance(plan.getSourceContainerId(),
                                                                          i,
                                                                          plan.getTargetContainerId(),
                                                                          plan.getTargetProcessId(),
                                                                          plan.getMappings());
        }
        List<Migration> migrations = migrationService.findAll();

        assertNotNull(migrations);
        assertEquals(1, migrations.size());
        Migration m = migrations.get(0);
        assertEquals(ExecutionStatus.COMPLETED, m.getStatus());
        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getStartedAt());
        assertNotNull(m.getFinishedAt());
        List<MigrationReport> results = migrationService.getResults(m.getId());
        assertEquals(110, results.size());
        results.stream().forEach(r -> assertTrue(r.getSuccessful()));
    }

    @Test
    public void testResumeMigration() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);
        Migration m = new Migration(def);
        getEntityManager().persist(m);
        m.getReports().add(new MigrationReport(m.getId(), createReport(1L)));

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);
        for (long i = 2; i <= 3; i++) {
            Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(i))).thenReturn(buildProcessInstance(i, plan.getSourceContainerId()));
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString())).thenReturn(mockQueryServicesClient);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        migrationService.migrate(m);
        getEntityManager().getTransaction().commit();

        // Then
        for (long i = 2; i <= 3; i++) {
            verify(mockProcessAdminServicesClient).migrateProcessInstance(plan.getSourceContainerId(),
                                                                          i,
                                                                          plan.getTargetContainerId(),
                                                                          plan.getTargetProcessId(),
                                                                          plan.getMappings());
        }
        List<Migration> migrations = migrationService.findAll();

        assertNotNull(migrations);
        assertEquals(1, migrations.size());
        m = migrations.get(0);
        assertEquals(ExecutionStatus.COMPLETED, m.getStatus());
        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getStartedAt());
        assertNotNull(m.getFinishedAt());
        List<MigrationReport> results = migrationService.getResults(m.getId());
        assertEquals(3, results.size());
        results.stream().forEach(r -> assertTrue(r.getSuccessful()));
    }

    @Test
    public void testSubmitAlreadyMigrated() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);
        for (long i = 1; i <= 2; i++) {
            Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(i))).thenReturn(buildProcessInstance(i, plan.getSourceContainerId()));
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }
        Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(3L))).thenReturn(null);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString())).thenReturn(mockQueryServicesClient);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        // Then
        for (long i = 1; i <= 2; i++) {
            verify(mockProcessAdminServicesClient).migrateProcessInstance(plan.getSourceContainerId(),
                                                                          i,
                                                                          plan.getTargetContainerId(),
                                                                          plan.getTargetProcessId(),
                                                                          plan.getMappings());
        }
        List<Migration> migrations = migrationService.findAll();

        assertNotNull(migrations);
        assertEquals(1, migrations.size());
        Migration m = migrations.get(0);
        assertEquals(ExecutionStatus.COMPLETED, m.getStatus());
        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getStartedAt());
        assertNotNull(m.getFinishedAt());
        List<MigrationReport> results = migrationService.getResults(m.getId());
        assertEquals(3, results.size());
        results.stream().forEach(r -> assertTrue(r.getSuccessful()));
        assertEquals(1, results.get(2).getLogs().size());
        assertEquals("Instance did not exist in source container. Migration skipped", results.get(2).getLogs().get(0));
    }

    @Test
    public void testSubmitAsyncMigration() throws InvalidMigrationException, MigrationNotFoundException, URISyntaxException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, new Execution().setType(ExecutionType.ASYNC).setCallbackUrl(new URI("http://test.com/callback")));

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        for (long i = 1; i <= 3; i++) {
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        Migration m = migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        verify(schedulerServiceMock, times(1)).scheduleMigration(m);
    }

    @Test
    public void testUpdateMigration() throws InvalidMigrationException, MigrationNotFoundException, URISyntaxException, ReScheduleException {
        // Given
        Plan plan = createPlan();
        Execution execution = new Execution()
                .setType(ExecutionType.ASYNC)
                .setScheduledStartTime(LocalDateTime.now().plusDays(2).toInstant(ZoneOffset.UTC))
                .setCallbackUrl(new URI("http://test.com/callback"));
        MigrationDefinition def = createMigrationDefinition(plan, execution);

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        for (long i = 1; i <= 3; i++) {
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        getEntityManager().getTransaction().begin();
        Migration m = migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        // When
        MigrationDefinition updatedMigrationDef = createMigrationDefinition(plan, execution);
        updatedMigrationDef.getExecution().setScheduledStartTime(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC));
        Migration updatedMigration = migrationService.update(m.getId(), updatedMigrationDef);

        // Then
        verify(schedulerServiceMock, times(1)).scheduleMigration(m);
        verify(schedulerServiceMock, times(1)).scheduleMigration(updatedMigration);
        assertEquals(updatedMigrationDef.getExecution().getScheduledStartTime(), migrationService.get(updatedMigration.getId()).getDefinition().getExecution().getScheduledStartTime());
    }

    @Test
    public void testSubmitProcessNotFound() throws InvalidMigrationException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);
        def.getProcessInstanceIds().clear();

        // Setup mocks
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.FALSE);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString()))
                .thenReturn(mockProcessAdminServicesClient);
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString()))
                .thenReturn(mockQueryServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        try {
            getEntityManager().getTransaction().begin();
            migrationService.submit(def);
            fail("Expected validation exception");
        } catch (ProcessNotFoundException e) {
            assertNotNull(e);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

    @Test
    public void testSubmitPlanNotFound() throws InvalidMigrationException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);
        def.setPlanId(9999L);

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        try {
            getEntityManager().getTransaction().begin();
            migrationService.submit(def);
            fail("Expected InvalidMigrationException due to wrong planId");
        } catch (InvalidMigrationException e) {
            assertNotNull(e);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

    @Test(expected = InvalidMigrationException.class)
    public void testSubmitValidationNullDefinition() throws InvalidMigrationException {
        migrationService.submit(null);
    }

    @Test(expected = InvalidMigrationException.class)
    public void testSubmitValidationNullPlan() throws InvalidMigrationException {
        migrationService.submit(new MigrationDefinition());
    }

    @Test
    public void testSubmitValidationInvalidKieServer() throws InvalidMigrationException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);
        def.setKieServerId("wrong kieServerId");

        // Setup mock
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        try {
            getEntityManager().getTransaction().begin();
            migrationService.submit(def);
            fail("Expected InvalidKieServerException due to wrong kieserverId");
        } catch (InvalidKieServerException e) {
            assertNotNull(e);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

    @Test
    public void testSubmitValidationKieServerIdNull() {
        MigrationDefinition def = new MigrationDefinition();
        def.setPlanId(1L);
        try {
            getEntityManager().getTransaction().begin();
            migrationService.submit(def);
            fail("Expected InvalidKieServerException due to wrong kieserverId");
        } catch (InvalidMigrationException e) {
            assertNotNull(e);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

    @Test
    public void testSubmitFailed() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        MigrationDefinition def = createMigrationDefinition(plan, SYNC);

        // Setup mocks
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);

        Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                           eq(1L),
                                                                           anyString(),
                                                                           anyString(),
                                                                           anyMapOf(String.class, String.class)))
                .thenThrow(new RuntimeException("Foo"));
        Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(1L))).thenReturn(buildProcessInstance(1L, plan.getSourceContainerId()));
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        for (long i = 2; i <= 3; i++) {
            Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(i))).thenReturn(buildProcessInstance(i, plan.getSourceContainerId()));
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }

        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString())).thenReturn(mockQueryServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        migrationService.submit(def);
        getEntityManager().getTransaction().commit();

        // Then
        for (long i = 1; i <= 3; i++) {
            verify(mockProcessAdminServicesClient).migrateProcessInstance(plan.getSourceContainerId(),
                                                                          i,
                                                                          plan.getTargetContainerId(),
                                                                          plan.getTargetProcessId(),
                                                                          plan.getMappings());
        }
        List<Migration> migrations = migrationService.findAll();

        assertNotNull(migrations);
        assertEquals(1, migrations.size());
        Migration m = migrations.get(0);
        assertEquals(ExecutionStatus.FAILED, m.getStatus());
        assertNotNull(m.getCreatedAt());
        assertNotNull(m.getStartedAt());
        assertNotNull(m.getFinishedAt());
        List<MigrationReport> results = migrationService.getResults(m.getId());
        assertEquals(3, results.size());
        results.stream().forEach(r -> {
            if (r.getProcessInstanceId().equals(1L)) {
                assertFalse(r.getSuccessful());
            } else {
                assertTrue(r.getSuccessful());
            }
        });
    }

    @Test
    public void testMigrateAsync() throws InvalidMigrationException, MigrationNotFoundException {
        // Given
        Plan plan = createPlan();
        Execution execution = new Execution();
        execution.setType(ExecutionType.ASYNC).setScheduledStartTime(LocalDateTime.now().plusDays(2).toInstant(ZoneOffset.UTC));
        MigrationDefinition def = createMigrationDefinition(plan, execution);
        Migration m = new Migration(def);
        getEntityManager().persist(m);

        // Setup mock
        Mockito.when(getEntityManager().find(Migration.class, 99L)).thenReturn(m);
        Mockito.doNothing().when(getEntityManager()).persist(m);
        ProcessAdminServicesClient mockProcessAdminServicesClient = Mockito.mock(ProcessAdminServicesClient.class);
        QueryServicesClient mockQueryServicesClient = Mockito.mock(QueryServicesClient.class);
        Mockito.when(kieServiceMock.existsProcessDefinition(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
        for (long i = 1; i <= 3; i++) {
            Mockito.when(mockQueryServicesClient.findProcessInstanceById(eq(i))).thenReturn(buildProcessInstance(i, plan.getSourceContainerId()));
            Mockito.when(mockProcessAdminServicesClient.migrateProcessInstance(anyString(),
                                                                               eq(i),
                                                                               anyString(),
                                                                               anyString(),
                                                                               anyMapOf(String.class, String.class)))
                    .thenReturn(createReport(i));
        }
        Mockito.when(kieServiceMock.getProcessAdminServicesClient(anyString())).thenReturn(mockProcessAdminServicesClient);
        Mockito.when(kieServiceMock.getQueryServicesClient(anyString())).thenReturn(mockQueryServicesClient);
        addMockConfigs(kieServiceMock);

        // When
        getEntityManager().getTransaction().begin();
        Migration result = migrationService.migrate(m);
        getEntityManager().getTransaction().commit();

        // Then
        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
        verify(getEntityManager(), times(5)).persist(argument.capture());
        List<MigrationReport> reports = argument.getAllValues().stream()
                .filter(o -> o.getClass().equals(MigrationReport.class))
                .map(o -> (MigrationReport) o)
                .sorted((a, b) -> a.getProcessInstanceId().compareTo(b.getProcessInstanceId()))
                .collect(Collectors.toList());
        for (Long i = 1L; i <= 3; i++) {
            verify(mockProcessAdminServicesClient).migrateProcessInstance(plan.getSourceContainerId(),
                                                                          i,
                                                                          plan.getTargetContainerId(),
                                                                          plan.getTargetProcessId(),
                                                                          plan.getMappings());
            MigrationReport r = reports.get(i.intValue() - 1);
            assertEquals(Long.valueOf(m.getId()), r.getMigrationId());
            assertEquals(Long.valueOf(i), r.getProcessInstanceId());
            assertTrue(r.getSuccessful());
        }
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getFinishedAt());
    }

    private MigrationDefinition createMigrationDefinition(Plan plan, Execution execution) {
        MigrationDefinition def = new MigrationDefinition();
        def.setPlanId(plan.getId());
        def.setKieServerId(MOCK_KIESERVER_ID);
        def.setExecution(execution);
        def.setProcessInstanceIds(Arrays.asList(1L, 2L, 3L));
        return def;
    }

    private Plan createPlan() {
        Plan plan = new Plan();
        plan.setSourceContainerId("containerId");
        plan.setName("name");
        plan.setTargetContainerId("targetContainerId");
        plan.setTargetProcessId("targetProcessId");
        plan.setDescription("description");
        Map<String, String> mappings = new HashMap<>();
        mappings.put("source1", "target1");
        mappings.put("source2", "target2");
        plan.setMappings(mappings);
        getEntityManager().getTransaction().begin();
        planService.create(plan);
        getEntityManager().getTransaction().commit();
        return plan;
    }

    private MigrationReportInstance createReport(Long id) {
        MigrationReportInstance report = new MigrationReportInstance();
        report.setSuccessful(Boolean.TRUE);
        report.setProcessInstanceId(id);
        return report;
    }

    private List<ProcessInstance> buildProcessInstances(long start, long end) {
        List<ProcessInstance> pis = new ArrayList<>();
        for (long id = start; id <= end; id++) {
            pis.add(buildProcessInstance(id, null));
        }
        return pis;
    }

    private ProcessInstance buildProcessInstance(long id, String containerId) {
        ProcessInstance instance = new ProcessInstance();
        instance.setId(id);
        instance.setContainerId(containerId);
        return instance;
    }
}
