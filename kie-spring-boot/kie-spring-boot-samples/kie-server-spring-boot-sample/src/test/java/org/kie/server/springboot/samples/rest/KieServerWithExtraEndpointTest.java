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

package org.kie.server.springboot.samples.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpResponse;
import org.kie.server.springboot.samples.KieServerApplication;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-jaxrstest.properties")
public class KieServerWithExtraEndpointTest {

    @LocalServerPort
    private int port;    
   
    private String user = "john";
    private String password = "john@pwd1";

    
    @Test
    public void testExtraEndpoint() {
        
        String extraEndpoint = "http://localhost:" + port + "/rest/extra";
        
        KieServerHttpRequest httpRequest =
                KieServerHttpRequest.newRequest(extraEndpoint, user, password)
                .followRedirects(true)
                .timeout(1000)
                .contentType("application/json")
                .accept("application/json");
        httpRequest.get();
        
        KieServerHttpResponse response = httpRequest.response();
        int responseCode = response.code();
        assertEquals(200, responseCode);               
    }

}
