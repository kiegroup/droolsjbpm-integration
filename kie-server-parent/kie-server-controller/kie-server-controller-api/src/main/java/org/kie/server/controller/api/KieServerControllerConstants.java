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

package org.kie.server.controller.api;

public final class KieServerControllerConstants {

    private KieServerControllerConstants() {}

    public static final String KIE_CONTROLLER_SWAGGER_DISABLED = "org.kie.server.controller.swagger.disabled";

    public static final String KIE_CONTROLLER_TEMPLATE_CACHE_TTL = "org.kie.server.controller.template.cache.ttl";

    public static final String KIE_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE =
            "org.kie.server.controller.openshift.prefer.kieserver.service";

    public static final String KIE_CONTROLLER_OCP_GLOBAL_DISCOVERY_ENABLED =
            "org.kie.server.controller.openshift.global.discovery.enabled";

}
