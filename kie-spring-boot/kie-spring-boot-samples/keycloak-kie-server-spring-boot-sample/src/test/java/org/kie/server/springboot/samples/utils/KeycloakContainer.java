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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.DockerClient;

public class KeycloakContainer extends GenericContainer<KeycloakContainer> {
    private static Logger logger = LoggerFactory.getLogger(KeycloakContainer.class);
    
    public static final String KEYCLOAK_ADMIN_USER = "admin";
    public static final String KEYCLOAK_ADMIN_PASSWORD = "admin";
    private static final String KEYCLOAK_AUTH_PATH = "/auth";
    
    private static final String KEYCLOAK_IMAGE = System.getProperty("keycloak.image");
    private static final String KEYCLOAK_VERSION = System.getProperty("keycloak.version");

    private static final int KEYCLOAK_PORT_HTTP = Integer.getInteger("keycloak.http.port");
    
    /**
     * Create a KeycloakContainer from image and version at system properties.
     * If that version is not found, it will use latest
     */
    public KeycloakContainer() {
        this(findImage());
    }

    /**
     * Create a KeycloakContainer by passing the full docker image name
     *
     * @param dockerImage Full docker image name, e.g. quay.io/keycloak/keycloak:9.0.3
     */
    public KeycloakContainer(String dockerImage) {
        super(dockerImage);
        withExposedPorts(KEYCLOAK_PORT_HTTP);
    }

    @Override
    protected void configure() {
        withEnv("KEYCLOAK_USER", KEYCLOAK_ADMIN_USER);
        withEnv("KEYCLOAK_PASSWORD", KEYCLOAK_ADMIN_PASSWORD);
    }

    public String getAuthServerUrl() {
        return String.format("http://%s:%s%s", getContainerIpAddress(), getMappedPort(KEYCLOAK_PORT_HTTP), KEYCLOAK_AUTH_PATH);
    }
    
    private static String findImage() {
        DockerClient client = DockerClientFactory.instance().client();
        String targetImage = KEYCLOAK_IMAGE + ":" + KEYCLOAK_VERSION;
        if (!client.listImagesCmd().withImageNameFilter(targetImage).exec().isEmpty()) { 
            logger.info("Found '{}' image, using it", targetImage);
            return targetImage;
        }
        logger.info("Not Found '{}' image, using latest", targetImage);
        return KEYCLOAK_IMAGE + ":latest";
    }
}
