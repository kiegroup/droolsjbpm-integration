/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class KieServerIntegrationTest extends RestJmsSharedBaseIntegrationTest {
    private static ReleaseId releaseId1 = new ReleaseId("foo.bar", "baz", "2.1.0.GA");

    private static final String CONTAINER_ID = "kie1";

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(releaseId1);
    }

    @Test
    public void testGetServerInfo() throws Exception {
        ServiceResponse<KieServerInfo> reply = client.getServerInfo();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        KieServerInfo info = reply.getResult();
        Assert.assertEquals(getServerVersion(), info.getVersion());
    }

    private String getServerVersion() {
        // use the property if specified and fallback to KieServerEnvironment if no property set
        return System.getProperty("kie.server.version", KieServerEnvironment.getVersion().toString());
    }

    @Before
    public void setupKieServer() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId1);
    }

    @Test
    public void testScanner() throws Exception {
        ServiceResponse<KieScannerResource> si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( ResponseType.SUCCESS, si.getType() );
        KieScannerResource info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
        
        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STARTED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, info.getStatus() );
        
        si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, info.getStatus() );
        
        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STOPPED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );
        
        si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );
        
        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.DISPOSED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
        
        si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
    }

    @Test
    public void testScannerScanNow() throws Exception {
        ServiceResponse<KieScannerResource> si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( ResponseType.SUCCESS, si.getType() );
        KieScannerResource info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.SCANNING, 0L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );

        si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.DISPOSED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );

        si = client.getScannerInfo(CONTAINER_ID);
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );
    }

    @Test
    public void testScannerStatusOnContainerInfo() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo(CONTAINER_ID);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        KieContainerResource kci = reply.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, kci.getScanner().getStatus() );

        ServiceResponse<KieScannerResource> si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STARTED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        KieScannerResource info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, info.getStatus() );

        kci = client.getContainerInfo( CONTAINER_ID ).getResult();
        Assert.assertEquals( KieScannerStatus.STARTED, kci.getScanner().getStatus() );
        Assert.assertEquals( 10000, kci.getScanner().getPollInterval().longValue() );

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STOPPED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, info.getStatus() );

        kci = client.getContainerInfo( CONTAINER_ID ).getResult();
        Assert.assertEquals( KieScannerStatus.STOPPED, kci.getScanner().getStatus() );

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.DISPOSED, 10000L));
        Assert.assertEquals( si.getMsg(), ResponseType.SUCCESS, si.getType() );
        info = si.getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, info.getStatus() );

        kci = client.getContainerInfo( CONTAINER_ID ).getResult();
        Assert.assertEquals( KieScannerStatus.DISPOSED, kci.getScanner().getStatus() );
    }

    @Test
    public void testScannerStartAndStop() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo(CONTAINER_ID);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        KieContainerResource kci = reply.getResult();
        Assert.assertEquals(KieScannerStatus.DISPOSED, kci.getScanner().getStatus());

        ServiceResponse<KieScannerResource> si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STARTED, 20L));
        Assert.assertEquals(si.getMsg(), ResponseType.SUCCESS, si.getType());
        KieScannerResource info = si.getResult();
        Assert.assertEquals(KieScannerStatus.STARTED, info.getStatus());

        kci = client.getContainerInfo(CONTAINER_ID).getResult();
        Assert.assertEquals(KieScannerStatus.STARTED, kci.getScanner().getStatus());
        Assert.assertEquals(20L, kci.getScanner().getPollInterval().longValue());

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STOPPED, 20L));
        Assert.assertEquals(si.getMsg(), ResponseType.SUCCESS, si.getType());
        info = si.getResult();
        Assert.assertEquals(KieScannerStatus.STOPPED, info.getStatus());

        kci = client.getContainerInfo(CONTAINER_ID).getResult();
        Assert.assertEquals(KieScannerStatus.STOPPED, kci.getScanner().getStatus());

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STARTED, 20L));
        Assert.assertEquals(si.getMsg(), ResponseType.SUCCESS, si.getType());
        info = si.getResult();
        Assert.assertEquals(KieScannerStatus.STARTED, info.getStatus());

        kci = client.getContainerInfo(CONTAINER_ID).getResult();
        Assert.assertEquals(KieScannerStatus.STARTED, kci.getScanner().getStatus());
        Assert.assertEquals(20L, kci.getScanner().getPollInterval().longValue());

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.STOPPED, 20L));
        Assert.assertEquals(si.getMsg(), ResponseType.SUCCESS, si.getType());
        info = si.getResult();
        Assert.assertEquals(KieScannerStatus.STOPPED, info.getStatus());

        si = client.updateScanner(CONTAINER_ID, new KieScannerResource(KieScannerStatus.DISPOSED, 10000L));
        Assert.assertEquals(si.getMsg(), ResponseType.SUCCESS, si.getType());
        info = si.getResult();
        Assert.assertEquals(KieScannerStatus.DISPOSED, info.getStatus());
    }

    @Test
    public void testConversationIdHandling() throws Exception {
        client.getContainerInfo(CONTAINER_ID);
        String conversationId = client.getConversationId();
        assertNotNull(conversationId);

        client.getContainerInfo(CONTAINER_ID);
        String afterNextCallConversationId = client.getConversationId();
        assertEquals(conversationId, afterNextCallConversationId);

        // complete conversation to start with new one
        client.completeConversation();

        client.getContainerInfo(CONTAINER_ID);
        afterNextCallConversationId = client.getConversationId();
        assertNotEquals(conversationId, afterNextCallConversationId);
    }
}
