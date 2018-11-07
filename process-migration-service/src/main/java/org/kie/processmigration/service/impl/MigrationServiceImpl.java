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

package org.kie.processmigration.service.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.kie.processmigration.model.Execution.ExecutionStatus;
import org.kie.processmigration.model.Execution.ExecutionType;
import org.kie.processmigration.model.Migration;
import org.kie.processmigration.model.MigrationDefinition;
import org.kie.processmigration.model.MigrationReport;
import org.kie.processmigration.model.Plan;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.processmigration.model.exceptions.InvalidMigrationException;
import org.kie.processmigration.model.exceptions.MigrationNotFoundException;
import org.kie.processmigration.model.exceptions.PlanNotFoundException;
import org.kie.processmigration.model.exceptions.ReScheduleException;
import org.kie.processmigration.service.KieService;
import org.kie.processmigration.service.MigrationService;
import org.kie.processmigration.service.PlanService;
import org.kie.processmigration.service.SchedulerService;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MigrationServiceImpl implements MigrationService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationServiceImpl.class);
    private static final List<ExecutionStatus> PENDING_STATUSES = Arrays.asList(ExecutionStatus.STARTED,
                                                                                ExecutionStatus.CREATED);

    @PersistenceContext
    private EntityManager em;

    @Inject
    private PlanService planService;

    @Inject
    private KieService kieService;

    @Inject
    private SchedulerService schedulerService;

    public void resumeMigrations(@Observes @Initialized(ApplicationScoped.class) Object event) {
        logger.info("Resuming ongoing migrations ...");
        TypedQuery<Migration> query = em.createNamedQuery("Migration.findByStatus", Migration.class);
        query.setParameter("statuses", PENDING_STATUSES);
        query.getResultList().stream().forEach(m -> {
            try {
                migrate(m);
            } catch (InvalidMigrationException e) {
                logger.warn("Unable to resume migration with id: " + m.getId(), e);
            }
        });
    }

    @Override
    public Migration get(Long id) throws MigrationNotFoundException {
        TypedQuery<Migration> query = em.createNamedQuery("Migration.findById", Migration.class);
        query.setParameter("id", id);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new MigrationNotFoundException(id);
        }
    }

    @Override
    public List<MigrationReport> getResults(Long id) throws MigrationNotFoundException {
        Migration m = get(id);
        TypedQuery<MigrationReport> query = em.createNamedQuery("MigrationReport.findByMigrationId", MigrationReport.class);
        query.setParameter("id", m.getId());
        return query.getResultList();
    }

    @Override
    public List<Migration> findAll() {
        return em.createNamedQuery("Migration.findAll", Migration.class).getResultList();
    }

    @Override
    @Transactional
    public Migration submit(MigrationDefinition definition) throws InvalidMigrationException {
        validateDefinition(definition);
        Migration migration = new Migration(definition);
        em.persist(migration);
        if (ExecutionType.SYNC.equals(definition.getExecution().getType())) {
            migrate(migration);
        } else {
            schedulerService.scheduleMigration(migration);
        }
        return migration;
    }

    @Override
    @Transactional
    public Migration delete(Long id) throws MigrationNotFoundException {
        Migration migration = get(id);
        em.remove(migration);
        return migration;
    }

    @Override
    @Transactional
    public Migration update(Long id, MigrationDefinition definition) throws MigrationNotFoundException, ReScheduleException, InvalidMigrationException {
        validateDefinition(definition);
        Migration migration = get(id);
        if (migration == null) {
            return null;
        }
        if (!ExecutionStatus.SCHEDULED.equals(migration.getStatus())) {
            throw new ReScheduleException("The migration is not scheduled and cannot be re-scheduled");
        }
        if (ExecutionType.SYNC.equals(definition.getExecution().getType())) {
            throw new ReScheduleException("The migration execution type MUST be ASYNC");
        }
        migration.setDefinition(definition);
        em.persist(migration);
        schedulerService.reScheduleMigration(migration);
        return migration;
    }

    @Override
    @Transactional
    public Migration migrate(Migration migration) throws InvalidMigrationException {
        try {
            Plan plan = planService.get(migration.getDefinition().getPlanId());
            migration = em.find(Migration.class, migration.getId());
            if (ExecutionStatus.CREATED.equals(migration.getStatus()) || ExecutionStatus.SCHEDULED.equals(migration.getStatus())) {
                em.persist(migration.start());
            }
            AtomicBoolean hasErrors = new AtomicBoolean(false);
            ProcessAdminServicesClient adminServicesClient = kieService
                                                                       .getProcessAdminServicesClient(migration.getDefinition().getKieserverId());
            List<Long> instanceIds = getInstancesToMigrate(migration);
            for (Long instanceId : instanceIds) {
                MigrationReportInstance reportInstance = null;
                try {
                    reportInstance = adminServicesClient.migrateProcessInstance(
                                                                                plan.getSourceContainerId(),
                                                                                instanceId,
                                                                                plan.getTargetContainerId(),
                                                                                plan.getTargetProcessId(),
                                                                                plan.getMappings());
                } catch (Exception e) {
                    logger.warn("Unable to migrate instanceID: " + instanceId, e);
                    if (reportInstance == null) {
                        reportInstance = buildReportFromError(instanceId, e);
                    }
                }
                if (!hasErrors.get() && !reportInstance.isSuccessful()) {
                    hasErrors.set(Boolean.TRUE);
                }
                em.persist(new MigrationReport(migration.getId(), reportInstance));
            }
            migration.complete(hasErrors.get());
        } catch (PlanNotFoundException e) {
            migration.fail(e);
            throw new InvalidMigrationException("The provided plan id does not exist: " + migration.getDefinition().getPlanId());
        } catch (Exception e) {
            logger.warn("Migration failed", e);
            migration.fail(e);
        } finally {
            em.persist(migration);
            if (ExecutionType.ASYNC.equals(migration.getDefinition().getExecution().getType()) &&
                migration.getDefinition().getExecution().getCallbackUrl() != null) {
                doCallback(migration);
            }
        }
        return migration;
    }

    private void doCallback(Migration migration) {
        URI callbackURI = null;
        try {
            callbackURI = migration.getDefinition().getExecution().getCallbackUrl();
            Response response = ClientBuilder.newClient()
                                             .target(callbackURI)
                                             .request(MediaType.APPLICATION_JSON)
                                             .buildPost(Entity.json(migration))
                                             .invoke();
            if (Status.OK.getStatusCode() == response.getStatus()) {
                logger.debug("Migration [{}] - Callback to {} replied successfully", migration.getId(), callbackURI);
            } else {
                logger.warn("Migration [{}] - Callback to {} replied with {}", migration.getId(), callbackURI, response.getStatus());
            }
        } catch (Exception e) {
            logger.error("Migration [{}] - Callback to {} failed.", migration.getId(), callbackURI, e);
        }
    }

    private void validateDefinition(MigrationDefinition definition) throws InvalidMigrationException {
        if (definition.getPlanId() == null) {
            throw new InvalidMigrationException("The Plan ID is mandatory");
        }
        if (StringUtils.isBlank(definition.getKieserverId())) {
            throw new InvalidMigrationException("The KIE Server ID is mandatory");
        }
        if (!kieService.getConfigs().containsKey(definition.getKieserverId())) {
            throw new InvalidKieServerException(definition.getKieserverId());
        }
        if (definition.getProcessInstanceIds().isEmpty()) {
            throw new InvalidMigrationException("The process instances IDs to migrate are mandatory");
        }
    }

    private List<Long> getInstancesToMigrate(Migration migration) {
        List<Long> instanceIds = migration.getDefinition().getProcessInstanceIds();
        if (migration.getReports() == null || migration.getReports().isEmpty()) {
            return instanceIds;
        }
        List<Long> migratedInstances = migration.getReports().stream().map(r -> r.getMigrationId()).collect(Collectors.toList());
        return instanceIds.stream().filter(id -> !migratedInstances.contains(id)).collect(Collectors.toList());
    }

    private MigrationReportInstance buildReportFromError(Long instanceId, Exception e) {
        MigrationReportInstance reportInstance = new MigrationReportInstance();
        reportInstance.setSuccessful(false);
        reportInstance.setLogs(Arrays.asList(e.getMessage()));
        reportInstance.setProcessInstanceId(instanceId);
        reportInstance.setStartDate(new Date());
        reportInstance.setEndDate(new Date());
        return reportInstance;
    }

}
