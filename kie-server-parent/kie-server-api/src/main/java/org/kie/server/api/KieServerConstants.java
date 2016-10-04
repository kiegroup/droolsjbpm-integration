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

package org.kie.server.api;

public class KieServerConstants {

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

    public static final String KIE_DROOLS_FILTER_REMOTEABLE_CLASSES = "org.drools.server.filter.classes";

    // kie server dedicated parameters
    public static final String KIE_SERVER_ID = "org.kie.server.id";
    public static final String KIE_SERVER_LOCATION = "org.kie.server.location";
    public static final String KIE_SERVER_JAAS_DOMAIN = "org.kie.server.domain";
    public static final String KIE_SERVER_CONTROLLER = "org.kie.server.controller";
    public static final String KIE_SERVER_STATE_REPO = "org.kie.server.repo";

    // configuration parameters
    public static final String CFG_PERSISTANCE_DS = "org.kie.server.persistence.ds";
    public static final String CFG_PERSISTANCE_TM = "org.kie.server.persistence.tm";
    public static final String CFG_PERSISTANCE_DIALECT = "org.kie.server.persistence.dialect";
    public static final String CFG_PERSISTANCE_DEFAULT_SCHEMA = "org.kie.server.persistence.schema";

    public static final String CFG_BYPASS_AUTH_USER = "org.kie.server.bypass.auth.user";

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

    // non kie server parameters but used by its extensions etc
    public static final String CFG_HT_CALLBACK = "org.jbpm.ht.callback";
    public static final String CFG_HT_CALLBACK_CLASS = "org.jbpm.ht.custom.callback";

    public static final String CFG_EXECUTOR_INTERVAL = "org.kie.executor.interval";
    public static final String CFG_EXECUTOR_POOL = "org.kie.executor.pool.size";
    public static final String CFG_EXECUTOR_RETRIES = "org.kie.executor.retry.count";
    public static final String CFG_EXECUTOR_TIME_UNIT = "org.kie.executor.timeunit";
    public static final String CFG_EXECUTOR_DISABLED = "org.kie.executor.disabled";

    public static final String CFG_DOCUMENT_STORAGE_PATH = "org.jbpm.document.storage";

    public static final String CFG_JBPM_TASK_CLEANUP_LISTENER = "org.jbpm.task.cleanup.enabled";
    public static final String CFG_JBPM_TASK_BAM_LISTENER = "org.jbpm.task.bam.enabled";
    public static final String CFG_JBPM_PROCESS_IDENTITY_LISTENER = "org.jbpm.process.identity.enabled";

    public static final String CFG_KIE_MVN_SETTINGS = "kie.maven.settings.custom";

    public static final String CFG_SYNC_DEPLOYMENT = "org.kie.server.sync.deploy";

    public static final String KIE_SERVER_PARAM_MODULE_METADATA = "KieModuleMetaData";

    public static final String CAPABILITY_BRM = "BRM"; // Business Rules Management
    public static final String CAPABILITY_BPM = "BPM"; // Business Process Management
    public static final String CAPABILITY_BPM_UI = "BPM-UI"; // Business Process Management UI
    public static final String CAPABILITY_BRP = "BRP"; // Business Resource Planning
    public static final String CAPABILITY_CASE = "CaseMgmt"; // Case Management
    
    public static final String FAILURE_REASON_PROP = "failure-reason";

    // case management constants
    public static final String CASE_DYNAMIC_NODE_TYPE_PROP = "nodeType";
    public static final String CASE_DYNAMIC_NAME_PROP = "name";
    public static final String CASE_DYNAMIC_DATA_PROP = "data";
    public static final String CASE_DYNAMIC_DESC_PROP = "description";
    public static final String CASE_DYNAMIC_ACTORS_PROP = "actors";
    public static final String CASE_DYNAMIC_GROUPS_PROP = "groups";

    public static final String KIE_SERVER_PERSISTENCE_UNIT_NAME = "org.jbpm.domain";

}
