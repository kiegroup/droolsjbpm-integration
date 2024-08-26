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

package org.kie.server.springboot.samples.utils;

import static java.util.Collections.singletonList;
import static org.kie.server.springboot.samples.utils.KeycloakContainer.KEYCLOAK_ADMIN_PASSWORD;
import static org.kie.server.springboot.samples.utils.KeycloakContainer.KEYCLOAK_ADMIN_USER;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.BARTLET;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.BARTLET_PW;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.JOHN;
import static org.kie.server.springboot.samples.utils.KeycloakSampleConstants.JOHN_PW;

import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class KeycloakFixture {

    private static final String ADMIN_CLI_CLIENT_ID = "admin-cli";
    private static final String MASTER_REALM = "master";
    private static final String CLIENT_ID = "springboot-app";
    private static final String PM = "PM";
    private static final String PRESIDENT = "President";
    
    /**
     * Configure Keycloak to run the tests
     * <ul>
     * <li>Uses default master realm</li>
     * <li>Creates client named springboot-app with AccessType set to public and enables direct access grants</li>
     * <li>Creates realm roles to be used in the tests (PM, President)</li>
     * <li>Creates two users:<li>
     * <ul>
     * <li>user named john and password john1 with PM role</li>
     * <li>user named Bartlet and password 123456 with President role</li>
     * </ul>
     * </ul>
     *
     * @param serverUrl Keycloak auth URL
     */
    public static void setup(String serverUrl) {
        RealmResource realmResource = createRealmClient(serverUrl);

        createUserRole(realmResource, JOHN, JOHN_PW, PM);
        createUserRole(realmResource, BARTLET, BARTLET_PW, PRESIDENT);
    }

    private static void createUserRole(RealmResource realmResource, String user, String password, String role) {
        addRoleToRealm(realmResource, role);
        
        String userId = createUser(realmResource, user, password);

        mapRoleToUser(realmResource, userId, role);
    }

    private static RealmResource createRealmClient(String serverUrl) {
        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                                                      .serverUrl(serverUrl)
                                                      .realm(MASTER_REALM)
                                                      .clientId(ADMIN_CLI_CLIENT_ID)
                                                      .username(KEYCLOAK_ADMIN_USER)
                                                      .password(KEYCLOAK_ADMIN_PASSWORD)
                                                      .build();

        RealmResource realmResource = keycloakAdminClient.realm(MASTER_REALM);

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(CLIENT_ID);
        client.setPublicClient(true);
        client.setDirectAccessGrantsEnabled(true);
        realmResource.clients().create(client);
        return realmResource;
    }

    private static void mapRoleToUser(RealmResource realmResource, String userId, String role) {
        RoleRepresentation roleRepr = realmResource.roles().get(role).toRepresentation();

        RoleMappingResource mappings = realmResource.users().get(userId).roles();
        mappings.realmLevel().add(singletonList(roleRepr));
    }

    private static String createUser(RealmResource realmResource, String user, String password) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(user);

        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(userRepresentation);
        String userId = CreatedResponseUtil.getCreatedId(response);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        UserResource userResource = usersResource.get(userId);
        userResource.resetPassword(passwordCred);
        return userId;
    }

    private static void addRoleToRealm(RealmResource realmResource, String realmRole) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(realmRole);
        realmResource.roles().create(role);
    }
}
