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

package org.kie.server.controller.common;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.kie.server.controller.api.KieServerControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebListener
public class SwaggerAPIScanner implements ServletContextListener {

    public static final Boolean SWAGGER_DISABLED = Boolean.parseBoolean(System.getProperty(KieServerControllerConstants.KIE_CONTROLLER_SWAGGER_DISABLED,
                                                                                           "false"));

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerAPIScanner.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (SWAGGER_DISABLED) {
            return;
        }

        LOGGER.info("Starting Swagger API discovery");
        OpenAPI oas = new OpenAPI();
        oas.info(getInfo());
        oas.addServersItem(new Server().url(sce.getServletContext().getContextPath() + "/rest"));

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(Stream.of("org.kie.server").collect(Collectors.toSet())); // chek packages here

        try {
            new JaxrsOpenApiContextBuilder()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private Info getInfo() {
        Info info = new Info();
        // version in general refers to major version of the project (kie server) though it uses minor as well to allow emergency type of changesinfo.setTitle("KIE-Server API");
        // must be updated with every major release
        info.setVersion("7.0");
        info.setTitle("Controller API");
        return info;
    }
}
