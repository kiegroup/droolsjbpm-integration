package org.kie.maven.takari.plugin;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.maven.plugin.InjectReactiveIntegrationTest;

public class InjectReactiveIntegrationTakariTest extends InjectReactiveIntegrationTest {

    private String projectName = "kjar-4-bytecode-inject";

    public InjectReactiveIntegrationTakariTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        super(builder);
    }

    @Before
    public void preparePom() throws Exception {
        prepareTakariPom(projectName);
    }

    @After
    public void restorePom() throws Exception {
        restoreKiePom(projectName);
    }

    @Test
    public void testBasicBytecodeInjection() throws Exception {
        super.testBasicBytecodeInjection();
    }

    @Test
    public void testBasicBytecodeInjectionSelected() throws Exception {
        super.testBasicBytecodeInjectionSelected();
    }
}
