/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services;

import java.nio.charset.Charset;
import java.util.UUID;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class Bootstrap implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        if (KieServerEnvironment.getServerId() == null) {
            String serverName = sce.getServletContext().getServletContextName() +"@"+ sce.getServletContext().getContextPath();
            String serverId = UUID.nameUUIDFromBytes(serverName.getBytes(Charset.forName("UTF-8"))).toString();

            KieServerEnvironment.setServerId(serverId.toString());
            KieServerEnvironment.setServerName(serverName);
        }


        logger.info("KieServer (id {} (name {})) started initialization process", KieServerEnvironment.getServerId(), KieServerEnvironment.getServerName());
        KieServerLocator.getInstance();
        logger.info("KieServer (id {}) started successfully", KieServerEnvironment.getServerId());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        KieServerImpl server = KieServerLocator.getInstance();

        server.destroy();

        logger.info("KieServer (id {}) destroyed successfully", KieServerEnvironment.getServerId());
    }
}
