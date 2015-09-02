/*
 * Copyright 2015 JBoss Inc
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

    // extensions control parameters
    public static final String KIE_DROOLS_SERVER_EXT_DISABLED = "org.drools.server.ext.disabled";
    public static final String KIE_JBPM_SERVER_EXT_DISABLED = "org.jbpm.server.ext.disabled";

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

    public static final String CFG_BYPASS_AUTH_USER = "org.kie.server.bypass.auth.user";

    public static final String CFG_KIE_USER = "org.kie.server.user";
    public static final String CFG_KIE_PASSWORD = "org.kie.server.pwd";

    public static final String CFG_KIE_CONTROLLER_USER = "org.kie.server.controller.user";
    public static final String CFG_KIE_CONTROLLER_PASSWORD = "org.kie.server.controller.pwd";

    // non kie server parameters but used by its extensions etc
    public static final String CFG_HT_CALLBACK = "org.jbpm.ht.callback";
    public static final String CFG_HT_CALLBACK_CLASS = "org.jbpm.ht.custom.callback";

    public static final String CFG_EXECUTOR_INTERVAL = "org.kie.executor.interval";
    public static final String CFG_EXECUTOR_POOL = "org.kie.executor.pool.size";
    public static final String CFG_EXECUTOR_RETRIES = "org.kie.executor.retry.count";
    public static final String CFG_EXECUTOR_TIME_UNIT = "org.kie.executor.timeunit";
    public static final String CFG_EXECUTOR_DISABLED = "org.kie.executor.disabled";

    public static final String CFG_KIE_MVN_SETTINGS = "kie.maven.settings.custom";


}
