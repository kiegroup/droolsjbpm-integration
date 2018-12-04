package org.kie.maven.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestUtil {

    private static final String PROJECT_VERSION_KEY = "project.version";

    private static volatile String projectVersion;

    public synchronized static String getProjectVersion() throws IOException {
        if (projectVersion == null) {
            try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.properties")) {
                final Properties properties = new Properties();
                properties.load(inputStream);
                projectVersion = properties.getProperty(PROJECT_VERSION_KEY);
            }
        }
        return projectVersion;
    }

    private TestUtil() {
        // It is forbidden to create instances of util classes.
    }
}
