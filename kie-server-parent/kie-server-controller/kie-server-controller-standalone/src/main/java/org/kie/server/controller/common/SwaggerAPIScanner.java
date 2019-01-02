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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.config.SwaggerScannerLocator;
import org.kie.server.controller.api.KieServerControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        JaxrsScanner jaxrsScanner = new DefaultJaxrsScanner();
        jaxrsScanner.setPrettyPrint(true);
        /*
         * Set our JAX-RS Scanner with SCANNER_ID_DEFAULT.
         * We need to do this before creating the BeanConfig, as this prevents the BeanConfig to register itself as the default scanner.
         * The first one wins.
         */
        SwaggerScannerLocator.getInstance().putScanner((SwaggerContextService.SCANNER_ID_DEFAULT), jaxrsScanner);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath(sce.getServletContext().getContextPath() + "/rest");
        beanConfig.setVersion("7.0");
        beanConfig.setTitle("Controller API");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
