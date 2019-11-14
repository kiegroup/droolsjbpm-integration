/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KieServerRouterAuthNoIdPTest {

    private KieServerRouter router;
    private File repository;

    @Before
    public void startStandaloneRouter() {
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());
        System.setProperty(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED, "true");
        System.setProperty(KieServerRouterConstants.KIE_ROUTER_IDENTITY_PROVIDER, "fake");

    }

    @After
    public void stopStandaloneRouter() {
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        System.clearProperty(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED);
        System.clearProperty(KieServerRouterConstants.KIE_ROUTER_IDENTITY_PROVIDER);
        router.stop(true);
        router = null;

        Stream.of(repository.listFiles()).forEach(f-> f.delete());
        repository.delete();
    }

    @Test
    public void testValidAuthMgmtKieServerRouter() throws Exception {
        router = new KieServerRouter(true, "fake");
        
        Throwable thrown = catchThrowable(() -> {
            router.start("localhost", 1234);
        });
        
        assertThat(thrown)
          .isInstanceOf(RuntimeException.class)
          .hasMessageMatching("Identity Provider .* not found !");
    }

    
}
