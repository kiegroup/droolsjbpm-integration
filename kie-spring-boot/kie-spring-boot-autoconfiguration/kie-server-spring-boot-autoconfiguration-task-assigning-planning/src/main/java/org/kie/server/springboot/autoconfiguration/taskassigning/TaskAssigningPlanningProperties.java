/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.springboot.autoconfiguration.taskassigning;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taskassigning")
public class TaskAssigningPlanningProperties {

    private ProcessRuntime processRuntime = new ProcessRuntime();

    private Solver solver = new Solver();

    private UserSystem userSystem = new UserSystem();

    private Core core = new Core();

    private int publishWindowSize;

    private String solutionSyncInterval;

    private String solutionSyncQueriesShift;

    private String usersSyncInterval;

    private RuntimeDelegate runtimeDelegate = new RuntimeDelegate();

    public int getPublishWindowSize() {
        return publishWindowSize;
    }

    public void setPublishWindowSize(int publishWindowSize) {
        this.publishWindowSize = publishWindowSize;
    }

    public ProcessRuntime getProcessRuntime() {
        return processRuntime;
    }

    public String getSolutionSyncInterval() {
        return solutionSyncInterval;
    }

    public void setSolutionSyncInterval(String solutionSyncInterval) {
        this.solutionSyncInterval = solutionSyncInterval;
    }

    public void setProcessRuntime(ProcessRuntime processRuntime) {
        this.processRuntime = processRuntime;
    }

    public String getSolutionSyncQueriesShift() {
        return solutionSyncQueriesShift;
    }

    public void setSolutionSyncQueriesShift(String solutionSyncQueriesShift) {
        this.solutionSyncQueriesShift = solutionSyncQueriesShift;
    }

    public String getUsersSyncInterval() {
        return usersSyncInterval;
    }

    public void setUsersSyncInterval(String usersSyncInterval) {
        this.usersSyncInterval = usersSyncInterval;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    public UserSystem getUserSystem() {
        return userSystem;
    }

    public void setUserSystem(UserSystem userSystem) {
        this.userSystem = userSystem;
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public RuntimeDelegate getRuntimeDelegate() {
        return runtimeDelegate;
    }

    public void setRuntimeDelegate(RuntimeDelegate runtimeDelegate) {
        this.runtimeDelegate = runtimeDelegate;
    }

    public class ProcessRuntime {

        private String url;
        private String user;
        private String pwd;
        private int timeout;
        private String targetUser;
        private Key key = new Key();

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public String getTargetUser() {
            return targetUser;
        }

        public void setTargetUser(String targetUser) {
            this.targetUser = targetUser;
        }

        public Key getKey() {
            return key;
        }

        public void setKey(Key key) {
            this.key = key;
        }
    }

    public class Key {

        private String alias;
        private String pwd;

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }
    }

    public class Solver {

        private String configResource;

        private String moveThreadCount;

        private int moveThreadBufferSize;

        private String threadFactoryClass;

        private Container container = new Container();

        public String getConfigResource() {
            return configResource;
        }

        public void setConfigResource(String configResource) {
            this.configResource = configResource;
        }

        public String getMoveThreadCount() {
            return moveThreadCount;
        }

        public void setMoveThreadCount(String moveThreadCount) {
            this.moveThreadCount = moveThreadCount;
        }

        public int getMoveThreadBufferSize() {
            return moveThreadBufferSize;
        }

        public void setMoveThreadBufferSize(int moveThreadBufferSize) {
            this.moveThreadBufferSize = moveThreadBufferSize;
        }

        public String getThreadFactoryClass() {
            return threadFactoryClass;
        }

        public void setThreadFactoryClass(String threadFactoryClass) {
            this.threadFactoryClass = threadFactoryClass;
        }

        public Container getContainer() {
            return container;
        }

        public void setContainer(Container container) {
            this.container = container;
        }
    }

    public class Container {

        private String id;
        private String groupId;
        private String artifactId;
        private String version;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public class Simple {

        private String users;
        private String skills;
        private String affinities;

        public String getUsers() {
            return users;
        }

        public void setUsers(String users) {
            this.users = users;
        }

        public String getSkills() {
            return skills;
        }

        public void setSkills(String skills) {
            this.skills = skills;
        }

        public String getAffinities() {
            return affinities;
        }

        public void setAffinities(String affinities) {
            this.affinities = affinities;
        }
    }

    public class UserSystem {

        private String name;
        private Container container = new Container();
        private Simple simple = new Simple();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Container getContainer() {
            return container;
        }

        public void setContainer(Container container) {
            this.container = container;
        }

        public Simple getSimple() {
            return simple;
        }

        public void setSimple(Simple simple) {
            this.simple = simple;
        }
    }

    public class Core {

        private Model model = new Model();

        public Model getModel() {
            return model;
        }

        public void setModel(Model model) {
            this.model = model;
        }
    }

    public class Model {

        private String planningUserId;

        public String getPlanningUserId() {
            return planningUserId;
        }

        public void setPlanningUserId(String planningUserId) {
            this.planningUserId = planningUserId;
        }
    }

    public class RuntimeDelegate {

        private int pageSize;

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }
}
