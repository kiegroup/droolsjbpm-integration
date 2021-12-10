/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.services.task.identity.AbstractUserGroupInfo;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.kie.api.task.UserGroupCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakCallbackImpl
        extends AbstractUserGroupInfo
        implements UserGroupCallback {

    public static final Logger logger = LoggerFactory.getLogger(KeycloakCallbackImpl.class);

    public static final String KEYCLOAK_CONFIG_FILE_KEY = "keycloak-config-file";
    public static final String KIE_GIT_FILE_SYSTEM_PROP = "org.uberfire.ext.security.keycloak.keycloak-config-file";
    public static final String DEFAULT_KIE_GIT_FILE_PATH = System.getProperty("jboss.home.dir") + "/kie-git.json";

    private final String configFile;

    public KeycloakCallbackImpl() {
        configFile = System.getProperty(KIE_GIT_FILE_SYSTEM_PROP, DEFAULT_KIE_GIT_FILE_PATH);
    }

    public boolean existsUser(String userId) {
        return true;
    }

    public boolean existsGroup(String groupId) {
        return true;
    }

    public List<String> getGroupsForUser(String userId) {
        Keycloak keycloak = Keycloak.getInstance(
                "http://localhost:8180/auth",
                "KieSSORealm",
                "kie",
                "f89bcbfc-4486-4ccb-be0e-1543d4b35ebf"
        );

        RealmsResource realms = keycloak.realms();

        UsersResource kieSSORealm = keycloak.realm("KieSSORealm").users();

        Set<String> result = new HashSet<>();

        return new ArrayList<>(result);
    }
}
