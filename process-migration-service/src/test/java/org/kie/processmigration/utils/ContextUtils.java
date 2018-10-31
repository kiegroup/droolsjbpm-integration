package org.kie.processmigration.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.wildfly.swarm.container.config.ConfigViewFactory;
import org.wildfly.swarm.container.config.ConfigViewImpl;
import org.wildfly.swarm.spi.api.config.ConfigView;

@ApplicationScoped
public class ContextUtils {

    @Produces
    private ConfigView createConfigView() throws IOException {
        URL url = getClass().getClassLoader().getResource("h2-defaults.yml");
        ConfigViewFactory factory = new ConfigViewFactory(new Properties());
        factory.load("test", url);
        ConfigViewImpl view = factory.get();
        view.activate();
        return view;
    }

}
