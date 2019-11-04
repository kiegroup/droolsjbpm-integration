/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router;

public class KieServerRouterConstants {
    public static final String ROUTER_ID = "org.kie.server.router.id";
    public static final String ROUTER_NAME = "org.kie.server.router.name";

    public static final String ROUTER_HOST = "org.kie.server.router.host";
    public static final String ROUTER_PORT = "org.kie.server.router.port";
    public static final String ROUTER_PORT_TLS = "org.kie.server.router.tls.port";
    public static final String ROUTER_EXTERNAL_URL = "org.kie.server.router.url.external";
    
    public static final String ROUTER_KEYSTORE = "org.kie.server.router.tls.keystore";
    public static final String ROUTER_KEYSTORE_PASSWORD = "org.kie.server.router.tls.keystore.password";
    public static final String ROUTER_KEYSTORE_KEYALIAS = "org.kie.server.router.tls.keystore.keyalias";

    public static final String ROUTER_REPOSITORY_DIR = "org.kie.server.router.repo";
    public static final String ROUTER_IDENTITY_FILE = "org.kie.server.router.identity.file";

    public static final String KIE_CONTROLLER = "org.kie.server.controller";
    public static final String KIE_CONTROLLER_USER = "org.kie.server.controller.user";
    public static final String KIE_CONTROLLER_PASSWORD = "org.kie.server.controller.pwd";
    public static final String KIE_CONTROLLER_TOKEN = "org.kie.server.controller.token";
    public static final String CONFIG_FILE_WATCHER_ENABLED = "org.kie.server.router.config.watcher.enabled";
    public static final String CONFIG_FILE_WATCHER_INTERVAL = "org.kie.server.router.config.watcher.interval";
    public static final String KIE_SERVER_CONTROLLER_ATTEMPT_INTERVAL = "org.kie.server.controller.retry.interval";
    public static final String KIE_SERVER_RECOVERY_ATTEMPT_LIMIT = "org.kie.server.recovery.retry.limit";

    public static final String KIE_ROUTER_MANAGEMENT_SECURED = "org.kie.server.router.management.password";
    public static final String KIE_ROUTER_IDENTITY_PROVIDER = "org.kie.router.identity.provider";
}
