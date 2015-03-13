package org.kie.server.services;

import java.nio.charset.Charset;
import java.util.UUID;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.kie.server.api.KieServerEnvironment;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class Bootstrap implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String serverIdSuffix = System.getProperty("org.kie.server.id.suffix");
        String serverIdString = sce.getServletContext().getServletContextName() +"@"+ sce.getServletContext().getContextPath();

        if (serverIdSuffix != null) {
            serverIdString = serverIdString+"_"+serverIdSuffix;
        }

        UUID serverId = UUID.nameUUIDFromBytes(serverIdString.getBytes(Charset.forName("UTF-8")));

        KieServerEnvironment.setServerId(serverId.toString());
        logger.info("KieServer (id {}) started initialization process", KieServerEnvironment.getServerId());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        KieServerImpl server = KieServerLocator.getInstance();

        server.destroy();

        logger.info("KieServer (id {}) destroyed successfully", KieServerEnvironment.getServerId());
    }
}
