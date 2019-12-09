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

package org.kie.server.integrationtests.router.rest;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.stream.Stream;

import io.undertow.security.idm.PasswordCredential;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;
import org.kie.server.router.identity.IdentityService;

public class KieServerRouterCommandTest {

    private KieServerRouter router;
    private File repository;

    @Before
    public void startStandaloneRouter() {
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());
        System.setProperty(KieServerRouterConstants.ROUTER_IDENTITY_FILE, new File("target/users.properties").getAbsolutePath());


        // setup and start router
        Integer port = allocatePort();
        router = new KieServerRouter(true, "default");
        router.start("localhost", port);
    }

    @After
    public void stopStandaloneRouter() {
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        System.clearProperty(KieServerRouterConstants.ROUTER_IDENTITY_FILE);
        router.stop(true);
        router = null;

        Stream.of(repository.listFiles()).forEach(f-> f.delete());
        repository.delete();
    }

    @Test
    public void testDefaultIdentityService() throws Exception {
        IdentityService service = router.getIdentityService();
        // add instance
        service.addKieServerInstance("simpleId", "simple password");
        Assert.assertNotNull(service.verify("simpleId", new PasswordCredential("simple password".toCharArray())));

        // remove instance
        service.removeKieServerInstance("simpleId");
        Assert.assertNull(service.verify("simpleId", new PasswordCredential("simple password".toCharArray())));
    }

    
    @Test
    public void testDefaultIdentityServiceFromMain() throws Exception {
        // add instance
        KieServerRouter.main(new String[]{"-" + KieServerRouter.CMD_ADD_USER, "mainId", "mainpw"});
        
        IdentityService service = router.getIdentityService();
        Assert.assertNotNull(service.verify("mainId", new PasswordCredential("mainpw".toCharArray())));

        // remove instance
        KieServerRouter.main(new String[]{"-" + KieServerRouter.CMD_REMOVE_USER, "mainId", "mainpw"});
        Assert.assertNull(service.verify("mainId", new PasswordCredential("mainpw".toCharArray())));
    }


    private static int allocatePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            // failed to dynamically allocate port, try to use hard coded one
            return 9783;
        }
    }
}
