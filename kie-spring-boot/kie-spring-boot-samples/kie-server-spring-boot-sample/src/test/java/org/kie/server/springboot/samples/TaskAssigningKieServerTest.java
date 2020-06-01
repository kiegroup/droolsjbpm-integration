package org.kie.server.springboot.samples;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-task-assigning-test.properties")
public class TaskAssigningKieServerTest {

    private static final String TASK_ASSIGNING_RUNTIME = "TaskAssigningRuntime";
    private static final String TASK_ASSIGNING_PLANNING = "TaskAssigningPlanning";

    @Autowired
    private KieServer kieServer;

    @Test
    public void taskAssigningExtensionsStarted() {
        Map<String, KieServerExtension> extensions = ((KieServerImpl) kieServer).getServerExtensions().stream()
                .collect(Collectors.toMap(KieServerExtension::getExtensionName, Function.identity()));
        assertExtensionInitialized(extensions, TASK_ASSIGNING_RUNTIME);
        assertExtensionInitialized(extensions, TASK_ASSIGNING_PLANNING);
    }

    private void assertExtensionInitialized(Map<String, KieServerExtension> extensions, String extensionName) {
        KieServerExtension extension = extensions.get(extensionName);
        assertNotNull("Extension " + extensionName + " was not found in current server", extension);
        assertTrue("Extension " + extensionName + " is expected to be active", extension.isActive());
        assertTrue("Extension " + extensionName + " is expected to be initialized", extension.isInitialized());
    }
}
