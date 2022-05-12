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

package org.kie.server.api;

public class KieServerConstants {

    public static final String KIE_JBPM_SERVER_CLIENT_FAILED_ENDPOINT_INTERVAL_CHECK = "org.kie.server.client.loadbalancer.failedEndpointIntervalCheck";
    public static final String IS_DISPOSE_CONTAINER_PARAM = "jBPMExtensionIsDisposeContainer";
    public static final String LOCATION_HEADER = "Location";

    public static final String CLASS_TYPE_HEADER = "X-KIE-ClassType";
    public static final String KIE_CONTENT_TYPE_HEADER = "X-KIE-ContentType";
    public static final String KIE_CONVERSATION_ID_TYPE_HEADER = "X-KIE-ConversationId";

    // extensions control parameters
    public static final String KIE_DROOLS_SERVER_EXT_DISABLED = "org.drools.server.ext.disabled";
    public static final String KIE_JBPM_SERVER_EXT_DISABLED = "org.jbpm.server.ext.disabled";
    public static final String KIE_JBPM_UI_SERVER_EXT_DISABLED = "org.jbpm.ui.server.ext.disabled";
    public static final String KIE_OPTAPLANNER_SERVER_EXT_DISABLED = "org.optaplanner.server.ext.disabled";
    public static final String KIE_CASE_SERVER_EXT_DISABLED = "org.jbpm.case.server.ext.disabled";
    public static final String KIE_DMN_SERVER_EXT_DISABLED = "org.kie.dmn.server.ext.disabled";
    public static final String KIE_SWAGGER_SERVER_EXT_DISABLED = "org.kie.swagger.server.ext.disabled";
    public static final String KIE_KAFKA_SERVER_EXT_DISABLED = "org.kie.kafka.server.ext.disabled";
    public static final String KIE_PROMETHEUS_SERVER_EXT_DISABLED = "org.kie.prometheus.server.ext.disabled";
    public static final String KIE_SCENARIO_SIMULATION_SERVER_EXT_DISABLED = "org.kie.scenariosimulation.server.ext.disabled";
    public static final String KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED = "org.kie.server.taskAssigning.planning.ext.disabled";
    public static final String KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED = "org.kie.server.taskAssigning.runtime.ext.disabled";
    public static final String KIE_JBPM_CLUSTER_SERVER_EXT_DISABLED = "org.kie.jbpm.cluster.server.ext.disabled";

    public static final String KIE_DROOLS_FILTER_REMOTEABLE_CLASSES = "org.drools.server.filter.classes";

    // kie server dedicated parameters
    public static final String KIE_SERVER_ID = "org.kie.server.id";
    public static final String KIE_SERVER_LOCATION = "org.kie.server.location";
    public static final String KIE_SERVER_JAAS_DOMAIN = "org.kie.server.domain";
    public static final String KIE_SERVER_CONTROLLER = "org.kie.server.controller";
    public static final String KIE_SERVER_STATE_REPO = "org.kie.server.repo";
    public static final String KIE_SERVER_STATE_REPO_TYPE_DEFAULT = "KieServerStateFileRepository";
    public static final String KIE_SERVER_STATE_REPO_TYPE_CLOUD = "KieServerStateCloudRepository";
    public static final String KIE_SERVER_STATE_REPO_TYPE_OPENSHIFT = "KieServerStateOpenShiftRepository";
    public static final String KIE_SERVER_STATE_IMMUTABLE = "org.kie.server.state.immutable";
    public static final String KIE_SERVER_STATE_IMMUTABLE_INIT = "org.kie.server.state.immutable.init";
    public static final String KIE_SERVER_CONTAINER_DEPLOYMENT = "org.kie.server.container.deployment";
    public static final String KIE_SERVER_CONTAINER_LOCATOR = "org.kie.server.container.locator";
    public static final String KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR = "org.kie.server.process.instance.container.locator";
    public static final String KIE_SERVER_ACTIVATE_POLICIES = "org.kie.server.policy.activate";
    public static final String KIE_SERVER_MGMT_API_DISABLED = "org.kie.server.mgmt.api.disabled";
    public static final String KIE_SERVER_STARTUP_STRATEGY = "org.kie.server.startup.strategy";
    public static final String KIE_SERVER_MODE = "org.kie.server.mode";
    public static final String KIE_SERVER_INCLUDE_STACKTRACE = "org.kie.server.stacktrace.included";
    public static final String KIE_SERVER_STRICT_ID_FORMAT = "org.kie.server.strict.id.format";
    public static final String KIE_SERVER_STRICT_JAVABEANS_SERIALIZERS = "org.kie.server.strict.javaBeans.serializers";
    public static final String KIE_SERVER_STRICT_JAXB_FORMAT = "org.kie.server.strict.jaxb.format";
    public static final String KIE_SERVER_IMAGESERVICE_MAX_NODES = "org.kie.server.service.image.max_nodes";
    public static final String KIE_SERVER_REST_MODE_READONLY = "org.kie.server.rest.mode.readonly";
    public static final String KIE_SERVER_NOTIFY_UPDATES_TO_CONTROLLERS = "org.kie.server.update.notifications.rest.enabled";
    // configuration parameters
    public static final String CFG_PERSISTANCE_DS = "org.kie.server.persistence.ds";
    public static final String CFG_PERSISTANCE_TM = "org.kie.server.persistence.tm";
    public static final String CFG_PERSISTANCE_DIALECT = "org.kie.server.persistence.dialect";
    public static final String CFG_PERSISTANCE_DEFAULT_SCHEMA = "org.kie.server.persistence.schema";

    public static final String CFG_BYPASS_AUTH_USER = "org.kie.server.bypass.auth.user";

    public static final String CFG_KIE_SERVER_RESPONSE_QUEUE = "kie.server.jms.queues.response";

    public static final String CFG_KIE_SERVER_CONTROLLER_CONNECT_INTERVAL = "org.kie.server.controller.connect";

    /**
     * security settings used to connect to KIE Server
     */
    public static final String CFG_KIE_USER = "org.kie.server.user";
    public static final String CFG_KIE_PASSWORD = "org.kie.server.pwd";
    public static final String CFG_KIE_TOKEN = "org.kie.server.token";

    /**
     * Security settings used to connect to KIE Server Controller
     */
    public static final String CFG_KIE_CONTROLLER_USER = "org.kie.server.controller.user";
    public static final String CFG_KIE_CONTROLLER_PASSWORD = "org.kie.server.controller.pwd";
    public static final String CFG_KIE_CONTROLLER_TOKEN = "org.kie.server.controller.token";
    public static final String CFG_KIE_CONTROLLER_TIMEOUT = "org.kie.server.controller.timeout";

    // non kie server parameters but used by its extensions etc
    public static final String CFG_HT_CALLBACK = "org.jbpm.ht.callback";
    public static final String CFG_HT_CALLBACK_CLASS = "org.jbpm.ht.custom.callback";

    public static final String CFG_EXECUTOR_INTERVAL = "org.kie.executor.interval";
    public static final String CFG_EXECUTOR_POOL = "org.kie.executor.pool.size";
    public static final String CFG_EXECUTOR_RETRIES = "org.kie.executor.retry.count";
    public static final String CFG_EXECUTOR_TIME_UNIT = "org.kie.executor.timeunit";
    public static final String CFG_EXECUTOR_JMS_QUEUE = "org.kie.executor.jms.queue";
    public static final String CFG_EXECUTOR_DISABLED = "org.kie.executor.disabled";

    public static final String CFG_DOCUMENT_STORAGE_PATH = "org.jbpm.document.storage";

    public static final String CFG_JBPM_TASK_CLEANUP_LISTENER = "org.jbpm.task.cleanup.enabled";
    public static final String CFG_JBPM_TASK_BAM_LISTENER = "org.jbpm.task.bam.enabled";
    public static final String CFG_JBPM_PROCESS_IDENTITY_LISTENER = "org.jbpm.process.identity.enabled";

    public static final String CFG_DEFAULT_QUERY_DEFS_LOCATION = "org.jbpm.query.definitions.location";

    public static final String CFG_KIE_MVN_SETTINGS = "kie.maven.settings.custom";

    public static final String CFG_SYNC_DEPLOYMENT = "org.kie.server.sync.deploy";

    public static final String CFG_SB_CXF_PATH = "org.kie.server.sb.cfg.cxf.path";

    public static final String KIE_SERVER_PARAM_MODULE_METADATA = "KieModuleMetaData";
    public static final String KIE_SERVER_PARAM_MESSAGES = "ContainerMessages";
    public static final String KIE_SERVER_PARAM_RESET_BEFORE_UPDATE = "KieServerResetBeforeUpdate";

    public static final String KIE_SERVER_ROUTER = "org.kie.server.router";
    public static final String KIE_ROUTER_MANAGEMENT_PASSWORD = "org.kie.server.router.management.password";
    public static final String KIE_ROUTER_MANAGEMENT_USERNAME = "org.kie.server.router.management.username";

    public static final String KIE_SERVER_ROUTER_ATTEMPT_INTERVAL = "org.kie.server.router.connect";

    public static final String KIE_OPTAPLANNER_THREAD_POOL_QUEUE_SIZE = "org.optaplanner.server.ext.thread.pool.queue.size";

    // ProcessConfig configuration item constants
    public static final String PCFG_RUNTIME_STRATEGY = "RuntimeStrategy";
    public static final String PCFG_KIE_BASE = "KBase";
    public static final String PCFG_KIE_SESSION = "KSession";
    public static final String PCFG_MERGE_MODE = "MergeMode";

    public static final String CAPABILITY_BRM = "BRM"; // Business Rules Management
    public static final String CAPABILITY_BPM = "BPM"; // Business Process Management
    public static final String CAPABILITY_BPM_UI = "BPM-UI"; // Business Process Management UI
    public static final String CAPABILITY_BRP = "BRP"; // Business Resource Planning
    public static final String CAPABILITY_CASE = "CaseMgmt"; // Case Management
    public static final String CAPABILITY_DMN = "DMN"; // DMN
    public static final String CAPABILITY_SWAGGER = "Swagger"; // Swagger
    public static final String CAPABILITY_BPM_KAFKA = "BPM-KAFKA"; // Business Process Management Kafka
    public static final String CAPABILITY_PROMETHEUS = "Prometheus"; // Prometheus
    public static final String CAPABILITY_SCENARIO_SIMULATION = "Scenario Simulation"; // Scenario Simulation
    public static final String CAPABILITY_TASK_ASSIGNING_RUNTIME = "TaskAssigningRuntime"; //Task Assigning Runtime
    public static final String CAPABILITY_JBPM_CLUSTER = "BPM-Cluster"; // Business Process Management cluster support
    
    public static final String FAILURE_REASON_PROP = "failure-reason";

    // case management constants
    public static final String CASE_DYNAMIC_NODE_TYPE_PROP = "nodeType";
    public static final String CASE_DYNAMIC_NAME_PROP = "name";
    public static final String CASE_DYNAMIC_DATA_PROP = "data";
    public static final String CASE_DYNAMIC_DESC_PROP = "description";
    public static final String CASE_DYNAMIC_ACTORS_PROP = "actors";
    public static final String CASE_DYNAMIC_GROUPS_PROP = "groups";

    public static final String CFG_CASE_ID_GENERATOR = "org.kie.server.cases.generator";

    public static final String KIE_SERVER_PERSISTENCE_UNIT_NAME = "org.jbpm.domain";

    public static final String QUERY_ORDER_BY = "q_order_by";
    public static final String QUERY_ASCENDING = "q_ascending";
    public static final String QUERY_ORDER_BY_CLAUSE = "q_order_by_clause";
    public static final String QUERY_COLUMN_MAPPING = "q_column_mapping";

    public static final String CFG_KIE_SERVER_JMS_SESSION_TX = "org.kie.server.jms.session.tx";
    public static final String CFG_KIE_SERVER_JMS_SESSION_ACK = "org.kie.server.jms.session.ack";

    // System variable to store the enabled packages for the XStreamMarshaller
    public static final String SYSTEM_XSTREAM_ENABLED_PACKAGES = "org.kie.server.xstream.enabled.packages";

    public static final String RESET_CONTAINER_BEFORE_UPDATE = "resetBeforeUpdate";

    public static final String JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR = "org.kie.server.json.customObjectDeserializerCNFEBehavior";
}
