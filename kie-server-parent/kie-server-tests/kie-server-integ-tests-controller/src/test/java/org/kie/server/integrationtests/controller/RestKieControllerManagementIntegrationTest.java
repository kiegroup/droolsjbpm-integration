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

package org.kie.server.integrationtests.controller;

import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;

import static org.junit.Assert.*;

public class RestKieControllerManagementIntegrationTest extends KieControllerManagementIntegrationTest<KieServerControllerHTTPClientException> {

    @Override
    protected void assertNotFoundException(KieServerControllerHTTPClientException e) {
        assertEquals(404,
                     e.getResponseCode());
        assertNotNull(e.getMessage());
    }

    @Override
    protected void assertBadRequestException(KieServerControllerHTTPClientException e) {
        assertEquals(400, e.getResponseCode());
        assertNotNull(e.getMessage());
    }

}
