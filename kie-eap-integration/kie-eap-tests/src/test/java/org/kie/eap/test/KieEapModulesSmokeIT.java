/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.eap.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.net.URL;

@RunWith(Arquillian.class)
public class KieEapModulesSmokeIT {
    private static final String RULE_PKG_NAME = "org.kie.eap.test";

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource("jboss-deployment-structure-drools-and-jbpm-modules.xml", "jboss-deployment-structure.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void deployWarWithDroolsAndJbpmModuleDependencies(@ArquillianResource URL baseUrl) {
        Assert.assertNotNull(baseUrl);
    }

    @Test
    public void createKieSessionFromSimpleDrlAndFireRules() {
        String drl = "package " + RULE_PKG_NAME + ";\n" +
                "\n" +
                "rule \"Dummy rule\"\n" +
                "when\n" +
                "    String( this == \"MyValue\" )\n" +
                "then\n" +
                "    System.out.println(\"Rule fired!\");\n" +
                "end";
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);
        KieBase kieBase = kieHelper.build();
        // can't easily use AssertJ as it is not available in the server classpath (this is in-container test)
        Assert.assertFalse("At least one KiePackage expected!", kieBase.getKiePackages().isEmpty());
        Assert.assertTrue("Expected KiePackage " + RULE_PKG_NAME + " not found in " + kieBase.getKiePackages(),
                kieBase.getKiePackages().stream().anyMatch(kp -> RULE_PKG_NAME.equals(kp.getName())) );

        KieSession kieSession = kieBase.newKieSession();
        kieSession.insert("MyValue");
        int rulesFired = kieSession.fireAllRules();
        Assert.assertEquals("Unexpected number of rules fired!", 1, rulesFired);
    }

}
