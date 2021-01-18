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

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class KeycloakContainer extends GenericContainer<KeycloakContainer> {
    public static final String KEYCLOAK_ADMIN_USER = "admin";
    public static final String KEYCLOAK_ADMIN_PASSWORD = "admin";
    private static final String KEYCLOAK_AUTH_PATH = "/auth";
    
    private static final String KEYCLOAK_IMAGE = System.getProperty("keycloak.image");
    private static final String KEYCLOAK_VERSION = System.getProperty("keycloak.version");

    private static final int KEYCLOAK_PORT_HTTP = Integer.getInteger("keycloak.http.port");
    
    public KeycloakContainer() {
        this(KEYCLOAK_IMAGE + ":" + KEYCLOAK_VERSION);
    }

    /**
     * Create a KeycloakContainer by passing the full docker image name and wait up to 2 minutes for being ready
     *
     * @param dockerImageName Full docker image name, e.g. quay.io/keycloak/keycloak:9.0.3
     */
    public KeycloakContainer(String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(KEYCLOAK_PORT_HTTP);
        setWaitStrategy(Wait
            .forHttp(KEYCLOAK_AUTH_PATH)
            .forPort(KEYCLOAK_PORT_HTTP)
            .withStartupTimeout(Duration.ofMinutes(2))
        );
    }

    @Override
    protected void configure() {
        withCommand(
            "-c standalone.xml", 
            "-b 0.0.0.0"
        );

        withEnv("KEYCLOAK_USER", KEYCLOAK_ADMIN_USER);
        withEnv("KEYCLOAK_PASSWORD", KEYCLOAK_ADMIN_PASSWORD);
    }

    public String getAuthServerUrl() {
        return String.format("http://%s:%s%s", getContainerIpAddress(), getMappedPort(KEYCLOAK_PORT_HTTP), KEYCLOAK_AUTH_PATH);
    }
}
