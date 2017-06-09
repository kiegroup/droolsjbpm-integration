/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.aries.blueprint.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieSession;
import org.kie.aries.blueprint.KieBlueprintContainer;
import org.kie.scanner.KieMavenRepository;

import static org.junit.Assert.assertTrue;

public class KieBlueprintScannerTest extends AbstractKieBlueprintDynamicModuleTest {

    private final int FIRST_VALUE = 5;
    private final int SECOND_VALUE = 10;

    private static BlueprintContainerImpl container = null;

    @Test
    public void testBlueprintKieScanner() throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieMavenRepository repository = createAndInstallModule( ks, FIRST_VALUE );

        container = createContainer();

        checkForValue(FIRST_VALUE);

        reinstallModule( repository, ks );

        KieScanner kscanner = (KieScanner)container.getComponentInstance( "blueprint-scanner-releaseId-scanner" );
        kscanner.scanNow();

        checkForValue(SECOND_VALUE);

        ks.getRepository().removeKieModule(releaseId);
    }

    public static BlueprintContainerImpl createContainer() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(KieBlueprintListenerTest.class.getResource("/org/kie/aries/blueprint/kie-scanner.xml"));
        return new KieBlueprintContainer( ClassLoader.getSystemClassLoader(), urls);
    }

    protected void reinstallModule( KieMavenRepository repository, KieServices ks ) throws IOException {
        InternalKieModule kJar2 = createKieJarWithClass(ks, releaseId, SECOND_VALUE);
        File kPom = createKPom( releaseId );
        repository.installArtifact(releaseId, kJar2, kPom);
    }

    protected void checkForValue(int value) {
        List<Integer> list = new ArrayList<Integer>();
        KieBase kieBase = (KieBase)container.getComponentInstance("KBase1");
        KieSession ksession = kieBase.newKieSession();

        ksession.setGlobal( "list", list );
        ksession.fireAllRules();
        ksession.dispose();
        assertTrue("Expected:<" + value + "> but was:<" + list.get(0)  + ">", list.get(0) == value);
    }

}
