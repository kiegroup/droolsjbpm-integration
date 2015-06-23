/*
 * Copyright 2015 JBoss Inc
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

package org.drools.jboss.integration;

import org.drools.jboss.integration.model.TestFactDeclaredInJar;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieSession;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@Ignore @RunWith(Arquillian.class)
public class KieArchiveTest {

    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver()
                           .loadPomFromFile("pom.xml").resolve("org.drools:drools-compiler",
                                                               "com.google.protobuf:protobuf-java")
                           .withTransitivity().asFile();

        JavaArchive kjar = ShrinkWrap.create(JavaArchive.class)
                .addClass(TestFactDeclaredInJar.class)
                .addAsResource("basic/sample.drl")
                .addAsResource("META-INF/kmodule.xml")
                .addAsResource("META-INF/maven/pom.properties");

        WebArchive drools = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(libs)
                .addAsLibrary(kjar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        System.out.println(drools.toString(true));
        return drools;
    }

    @Inject
    @KSession("basicKSession")
    KieSession basicKieSession;

    @Test
    public void test() {
        assertNotNull(basicKieSession);

        List list = new ArrayList();
        basicKieSession.setGlobal("resultList", list);

        basicKieSession.insert(new TestFactDeclaredInJar());
        basicKieSession.fireAllRules();

        assertEquals(2, list.size());
    }

}
