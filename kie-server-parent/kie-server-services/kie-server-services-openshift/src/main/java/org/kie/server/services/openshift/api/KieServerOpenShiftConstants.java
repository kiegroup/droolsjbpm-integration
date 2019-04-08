/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.openshift.api;

public class KieServerOpenShiftConstants {

    public static final String CFG_MAP_DATA_KEY = "kie-server-state";
    public static final String CFG_MAP_LABEL_SERVER_ID_KEY = "services.server.kie.org/kie-server-id";
    public static final String CFG_MAP_LABEL_SERVER_STATE_KEY = "services.server.kie.org/kie-server-state";
    public static final String CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED = "DETACHED";
    public static final String CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE = "IMMUTABLE";
    public static final String CFG_MAP_LABEL_SERVER_STATE_VALUE_USED = "USED";
    public static final String CFG_MAP_LABEL_APP_NAME_KEY = "application";
    public static final String CFG_MAP_NAME_SYNTHETIC_NAME = "kieserver";
    public static final String ENV_HOSTNAME = "HOSTNAME";
    public static final String KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME = "org.kie.server.services.openshift.service.name";
    public static final String ROLLOUT_REQUIRED = "services.server.kie.org/openshift-startup-strategy.rolloutRequired";
    public static final String STATE_CHANGE_TIMESTAMP = "services.server.kie.org/kie-server-state.changeTimestamp";
    public static final String UNKNOWN = "unknown";

    private KieServerOpenShiftConstants() {}
}
