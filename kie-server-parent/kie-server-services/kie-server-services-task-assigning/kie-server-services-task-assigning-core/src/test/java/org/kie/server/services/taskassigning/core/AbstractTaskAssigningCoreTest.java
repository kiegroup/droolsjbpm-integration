/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.kie.server.services.taskassigning.core.model.DefaultTaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.OrganizationalEntity;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

public abstract class AbstractTaskAssigningCoreTest extends AbstractTaskAssigningTest {

    protected boolean writeTestFiles() {
        return Boolean.parseBoolean(System.getProperty("org.kie.server.services.taskassigning.test.writeFiles", "false"));
    }

    protected SolverConfig createBaseConfig() {
        SolverConfig config = new SolverConfig();
        config.setSolutionClass(DefaultTaskAssigningSolution.class);
        config.setEntityClassList(Arrays.asList(TaskOrUser.class, Task.class));
        config.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig().withScoreDrls("org/kie/server/services/taskassigning/solver/taskAssigningDefaultScoreRules.drl"));
        return config;
    }

    protected Solver<TaskAssigningSolution<?>> createDaemonSolver() {
        SolverConfig config = createBaseConfig();
        config.setDaemon(true);
        SolverFactory<TaskAssigningSolution<?>> solverFactory = SolverFactory.create(config);
        return solverFactory.buildSolver();
    }

    protected Solver<TaskAssigningSolution<?>> createNonDaemonSolver(int stepCountLimit) {
        SolverConfig config = createBaseConfig();
        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        constructionHeuristicPhaseConfig.setConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT);
        LocalSearchPhaseConfig phaseConfig = new LocalSearchPhaseConfig();
        phaseConfig.setTerminationConfig(new TerminationConfig().withStepCountLimit(stepCountLimit));
        config.setPhaseConfigList(Arrays.asList(constructionHeuristicPhaseConfig, phaseConfig));
        SolverFactory<TaskAssigningSolution<?>> solverFactory = SolverFactory.create(config);
        return solverFactory.buildSolver();
    }

    protected TaskAssigningSolution<?> readTaskAssigningSolution(String resource) throws IOException {
        int index = resource.lastIndexOf("/");
        String prefix = resource;
        if (index >= 0) {
            prefix = resource.substring(index + 1);
        }
        File f = File.createTempFile(prefix, null);
        InputStream resourceAsStream = getClass().getResourceAsStream(resource);
        FileUtils.copyInputStreamToFile(resourceAsStream, f);
        XStreamSolutionFileIO<DefaultTaskAssigningSolution> solutionFileIO = new XStreamSolutionFileIO<>(DefaultTaskAssigningSolution.class);
        return solutionFileIO.read(f);
    }

    private static void appendln(StringBuilder builder) {
        builder.append('\n');
    }

    private static void appendln(StringBuilder builder, String text) {
        builder.append(text);
        appendln(builder);
    }

    public static void printSolution(TaskAssigningSolution<?> solution, StringBuilder builder) {
        solution.getUserList().forEach(taskOrUser -> {
            appendln(builder, "------------------------------------------");
            appendln(builder, printUser(taskOrUser));
            appendln(builder, "------------------------------------------");
            appendln(builder);
            Task task = taskOrUser.getNextTask();
            while (task != null) {
                builder.append(" -> ");
                appendln(builder, printTask(task));
                task = task.getNextTask();
                if (task != null) {
                    appendln(builder);
                }
            }
            appendln(builder);
        });
    }

    public static String printSolution(TaskAssigningSolution<?> solution) {
        StringBuilder builder = new StringBuilder();
        printSolution(solution, builder);
        return builder.toString();
    }

    public static String printUser(User user) {
        return "User{" +
                "id=" + user.getId() +
                ", entityId='" + user.getEntityId() + '\'' +
                ", groups=" + printOrganizationalEntities(user.getGroups()) +
                '}';
    }

    public static String printTask(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append(task.getName() +
                               ", pinned: " + task.isPinned() +
                               ", priority: " + task.getPriority() +
                               ", startTimeInMinutes: " + task.getStartTimeInMinutes() +
                               ", durationInMinutes:" + task.getDurationInMinutes() +
                               ", endTimeInMinutes: " + task.getEndTimeInMinutes() +
                               ", user: " + task.getUser().getEntityId() +
                               ", potentialOwners: " + printOrganizationalEntities(task.getPotentialOwners()));
        return builder.toString();
    }

    public static String printOrganizationalEntities(Set<? extends OrganizationalEntity> potentialOwners) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (potentialOwners != null) {
            potentialOwners.forEach(organizationalEntity -> {
                if (builder.length() > 1) {
                    builder.append(", ");
                }
                if (organizationalEntity.isUser()) {
                    builder.append("user = " + organizationalEntity.getEntityId());
                } else {
                    builder.append("group = " + organizationalEntity.getEntityId());
                }
            });
        }
        builder.append("}");
        return builder.toString();
    }

    public static void writeToTempFile(String fileName, String content) throws IOException {
        File tmpFile = File.createTempFile(fileName, null);
        Files.write(tmpFile.toPath(), content.getBytes());
    }
}