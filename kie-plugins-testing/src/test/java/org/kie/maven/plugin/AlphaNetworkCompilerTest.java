/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.maven.plugin;

import java.lang.reflect.Constructor;
import java.util.List;

import io.takari.maven.testing.executor.MavenRuntime;
import org.drools.ancompiler.CompiledNetwork;
import org.drools.ancompiler.ObjectTypeNodeCompiler;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.modelcompiler.CanonicalKieModule;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlphaNetworkCompilerTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";
    private final static String ARTIFACT_ID = "kie-maven-plugin-test-kjar-13";
    private final static String VERSION = "1.0.0.Final";
    private final static String KJAR_NAME = "kjar-13-with-compiledalphanetwork";

    public AlphaNetworkCompilerTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testAlphaNetworkCompiler() throws Exception {

        buildKJarProject(KJAR_NAME,
                         new String[]{"-Dorg.kie.version=" + TestUtil.getProjectVersion() },
                         "clean", "install");

        final KieServices kieServices = KieServices.get();
        final ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSession kSession = null;
        try {
            KieModule kieModule = ((KieContainerImpl)kieContainer).getKieModuleForKBase("kbase-compiled-alphanetwork");
            InternalKieModule internalKieModule;
            if (kieModule instanceof CanonicalKieModule) {
                internalKieModule = ((CanonicalKieModule) kieModule).getInternalKieModule();
            } else {
                internalKieModule = (InternalKieModule) kieModule;
            }
            String ancFileName = CanonicalKieModule.getANCFile(releaseId);
            assertEquals("META-INF/kie/org/kie/" + ARTIFACT_ID + "/alpha-network-compiler", ancFileName);
            assertTrue(internalKieModule.isAvailable(ancFileName));

            kSession = kieContainer.newKieSession("kbase-compiled-alphanetwork.session");

            ClassLoader classLoader = kieContainer.getClassLoader();
            Class<?> aClass = Class.forName("org.compiledalphanetwork.Person", true, classLoader);
            Constructor<?> constructor = aClass.getConstructor(String.class);
            Object lucaFactA = constructor.newInstance("Luca");

            kSession.insert(lucaFactA);
            int rulesFired = kSession.fireAllRules();
            kSession.dispose();

            assertEquals(1, rulesFired);

            assertReteIsAlphaNetworkCompiled(kSession);

        } finally {
            kSession.dispose();
        }
    }

    protected void assertReteIsAlphaNetworkCompiled(KieSession ksession) {
        Rete rete = ((InternalKnowledgeBase) ksession.getKieBase()).getRete();
        List<ObjectTypeNode> objectTypeNodes = ObjectTypeNodeCompiler.objectTypeNodes(rete);
        for(ObjectTypeNode otn : objectTypeNodes) {
            ObjectSinkPropagator objectSinkPropagator = otn.getObjectSinkPropagator();
            System.out.println(objectSinkPropagator.getClass().getCanonicalName());
            assertTrue(objectSinkPropagator instanceof CompiledNetwork);
        }
    }
}

