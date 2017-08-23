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
package org.kie.server.services.impl.storage.file;

import static org.junit.Assert.assertEquals;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_CONTAINER_DEPLOYMENT;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_REPO;
import static org.kie.server.api.model.KieContainerStatus.STARTED;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.impl.storage.KieServerState;

public class KieServerStateFileInitTest {

    private static final String letters = "letters";
    private static final String test = "test";
    private static final String example = "example";

    private static final ReleaseId gav0 = newReleaseId("abc.def:ghi:9.0.1.GA");
    private static final ReleaseId gav1 = newReleaseId("com.test:foo:1.0.0-SNAPSHOT");
    private static final ReleaseId gav2 = newReleaseId("com.test:foo:1.0.0.Final");
    private static final ReleaseId gav3 = newReleaseId("com.test:foo:2.0.0-SNAPSHOT");
    private static final ReleaseId gav4 = newReleaseId("com.test:foo:2.0.0.Alpha1");
    private static final ReleaseId gav5 = newReleaseId("com.test:foo:2.0.0.Beta2");
    private static final ReleaseId gav6 = newReleaseId("org.example:test:0.0.1-SNAPSHOT");
    private static final ReleaseId gav7 = newReleaseId("org.example:test:1.0");

    private static final String serverContainerDeployment;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append(letters).append('=').append(gav0.toExternalForm()).append('|');
        for (ReleaseId gav : new ReleaseId[]{gav1, gav2, gav3, gav4, gav5}) {
            sb.append(test).append('=').append(gav.toExternalForm()).append('|');
        }
        sb.append(example).append('=').append(gav6.toExternalForm()).append('|');
        sb.append(example).append('=').append(gav7.toExternalForm());
        serverContainerDeployment = sb.toString();
    }

    private static final ReleaseId newReleaseId(String releaseId) {
        String[] gav = releaseId.split(":");
        return KieServices.Factory.get().newReleaseId(gav[0], gav[1], gav[2]);
    }

    private String origServerRepo = null;
    private String origServerId = null;
    private String origServerContainerDeployment = null;
    private File tempServerStateFile = null;

    @Before
    public void before() throws Exception {
        origServerRepo = System.getProperty(KIE_SERVER_STATE_REPO);
        origServerId = System.getProperty(KIE_SERVER_ID);
        origServerContainerDeployment = System.getProperty(KIE_SERVER_CONTAINER_DEPLOYMENT);
        tempServerStateFile = File.createTempFile("kieserver-", ".xml");
        // KieServerStateFileInit.init() will not clobber, so we only do the above to get a temp location, then delete it
        tempServerStateFile.delete();
        System.setProperty(KIE_SERVER_STATE_REPO, getServerRepo(tempServerStateFile));
        System.setProperty(KIE_SERVER_ID, getServerId(tempServerStateFile));
        System.setProperty(KIE_SERVER_CONTAINER_DEPLOYMENT, serverContainerDeployment);
    }

    @Test
    public void testInit() throws Exception {
        File serverStateFile = KieServerStateFileInit.init();
        String serverRepo = getServerRepo(serverStateFile);
        String serverId = getServerId(serverStateFile);
        KieServerStateFileRepository repository = new KieServerStateFileRepository(new File(serverRepo));
        KieServerState serverState = repository.load(getServerId(serverStateFile));

        KieServerConfig config = serverState.getConfiguration();
        assertEquals(serverRepo, config.getConfigItem(KIE_SERVER_STATE_REPO).getValue());
        assertEquals(serverId, config.getConfigItem(KIE_SERVER_ID).getValue());
        assertEquals(serverContainerDeployment, config.getConfigItem(KIE_SERVER_CONTAINER_DEPLOYMENT).getValue());

        Iterator<KieContainerResource> containers = serverState.getContainers().iterator();

        KieContainerResource exampleContainer = containers.next();
        assertEquals(example, exampleContainer.getContainerId());
        assertEquals(gav7.toExternalForm(), exampleContainer.getReleaseId().toExternalForm());
        assertEquals(STARTED, exampleContainer.getStatus());

        KieContainerResource lettersContainer = containers.next();
        assertEquals(letters, lettersContainer.getContainerId());
        assertEquals(gav0.toExternalForm(), lettersContainer.getReleaseId().toExternalForm());
        assertEquals(STARTED, lettersContainer.getStatus());

        KieContainerResource testContainer = containers.next();
        assertEquals(test, testContainer.getContainerId());
        assertEquals(gav5.toExternalForm(), testContainer.getReleaseId().toExternalForm());
        assertEquals(STARTED, testContainer.getStatus());
    }

    private String getServerRepo(File serverStateFile) throws Exception {
        return serverStateFile.getParentFile().getCanonicalPath();
    }

    private String getServerId(File serverStateFile) {
        return serverStateFile.getName().substring(0,  serverStateFile.getName().length() - 4);
    }

    @After
    public void after() throws Exception {
        if (origServerRepo != null) {
            System.setProperty(KIE_SERVER_STATE_REPO, origServerRepo);
        } else {
            System.clearProperty(KIE_SERVER_STATE_REPO);
        }
        if (origServerId != null) {
            System.setProperty(KIE_SERVER_ID, origServerId);
        } else {
            System.clearProperty(KIE_SERVER_ID);
        }
        if (origServerContainerDeployment != null) {
            System.setProperty(KIE_SERVER_CONTAINER_DEPLOYMENT, origServerContainerDeployment);
        } else {
            System.clearProperty(KIE_SERVER_CONTAINER_DEPLOYMENT);
        }
        tempServerStateFile.delete();
    }

}
