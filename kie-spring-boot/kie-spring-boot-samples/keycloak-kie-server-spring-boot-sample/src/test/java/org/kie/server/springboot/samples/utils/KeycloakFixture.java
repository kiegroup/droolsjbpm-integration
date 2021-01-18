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

import javax.ws.rs.core.Response;

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
    private static final String ROLE = "PM";
    
    /**
     * Configure Keycloak to run the tests
     * <ul>
     * <li>Uses default master realm</li>
     * <li>Creates client named springboot-app with AccessType set to public and enables direct access grants</li>
     * <li>Creates realm role that it is used in the example (PM)</li>
     * <li>Creates user named john and password john1 and adds PM role to that user</li>
     * </ul>
     *
     * @param serverUrl Keycloak auth URL
     * @param user username to be provisioned in Keycloak
     * @param password password associated to the provisioned user
     */
    public static void setup(String serverUrl, String user, String password) {
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

        RoleRepresentation rolePM = new RoleRepresentation();
        rolePM.setName(ROLE);
        realmResource.roles().create(rolePM);

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

        RoleRepresentation role = realmResource.roles().get(ROLE).toRepresentation();

        RoleMappingResource mappings = realmResource.users().get(userId).roles();
        mappings.realmLevel().add(singletonList(role));
    }
}
