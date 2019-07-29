/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;

public class KieServerContainerDeploymentTest {

    private static final String letters = "letters";
    private static final String test = "test";
    private static final String example = "example";
    private static final String robert = "robert";
    private static final String bob = "bob";

    private static final ReleaseId gav0 = newReleaseId("abc.def:ghi:9.0.1.GA");
    private static final ReleaseId gav1 = newReleaseId("com.test:foo:1.0.0-SNAPSHOT");
    private static final ReleaseId gav2 = newReleaseId("com.test:foo:1.0.0.Final");
    private static final ReleaseId gav3 = newReleaseId("com.test:foo:2.0.0-SNAPSHOT");
    private static final ReleaseId gav4 = newReleaseId("com.test:foo:2.0.0.Alpha1");
    private static final ReleaseId gav5 = newReleaseId("com.test:foo:2.0.0.Beta2");
    private static final ReleaseId gav6 = newReleaseId("org.example:test:0.0.1-SNAPSHOT");
    private static final ReleaseId gav7 = newReleaseId("org.example:test:1.0");
    private static final ReleaseId gav8 = newReleaseId("net.names:who:1.0.0.Final");

    private static final String canonicalRepresentation = "example=org.example:test:1.0|letters=abc.def:ghi:9.0.1.GA|robert(bob)=net.names:who:1.0.0.Final|test=com.test:foo:2.0.0.Beta2";
    private static final String serverContainerDeployment;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append(letters).append('=').append(gav0.toExternalForm()).append('|');
        for (ReleaseId gav : new ReleaseId[]{gav1, gav2, gav3, gav4, gav5}) {
            sb.append(test).append('=').append(gav.toExternalForm()).append('|');
        }
        sb.append(example).append('=').append(gav6.toExternalForm()).append('|');
        sb.append(example).append('=').append(gav7.toExternalForm()).append('|');
        sb.append(robert).append("(").append(bob).append(")=").append(gav8.toExternalForm());
        serverContainerDeployment = sb.toString();
    }

    private static final ReleaseId newReleaseId(String releaseId) {
        String[] gav = releaseId.split(":");
        return KieServices.Factory.get().newReleaseId(gav[0], gav[1], gav[2]);
    }

    @Test
    public void testDeploymentFiltering() throws Exception {
        Set<KieServerContainerDeployment> deployments = KieServerContainerDeployment.fromString(serverContainerDeployment, false);
        assertEquals(9, deployments.size());
        deployments = KieServerContainerDeployment.fromString(serverContainerDeployment, true);
        assertEquals(4, deployments.size());
        deployments = KieServerContainerDeployment.fromString(serverContainerDeployment);
        assertEquals(4, deployments.size());
    }

    @Test
    public void testDeploymentRepresentation() throws Exception {
        Set<KieServerContainerDeployment> deploymentsA = KieServerContainerDeployment.fromString(serverContainerDeployment);
        assertEquals(4, deploymentsA.size());
        String out = KieServerContainerDeployment.toString(deploymentsA);
        assertEquals(canonicalRepresentation, out);
        Set<KieServerContainerDeployment> deploymentsB = KieServerContainerDeployment.fromString(out);
        assertEquals(4, deploymentsB.size());
        assertEquals(deploymentsA, deploymentsB);
    }

}
