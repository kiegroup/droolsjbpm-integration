/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.ui;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.services.api.model.NodeInstanceDesc;
import org.junit.Test;

public class ImageServiceBaseTest {

    @Test
    public void testHighValueSubProcessInstanceId() {

        ImageServiceBase imageServiceBase = new ImageServiceBase();
        NodeInstanceDesc node = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("9999", 
                "1234", 
                "Test node", 
                "SubProcessNode", 
                "test-deployment", 
                1001L, 
                new Date(), 
                "test-connection", 
                1, 
                2001L, 
                3001L, 
                "", 
                null, 
                null);
        Map<String, String> subProcessLinks = new HashMap<>();

        imageServiceBase.populateSubProcessLink("test", node, subProcessLinks);
        
        assertEquals(1, subProcessLinks.size());
        assertEquals("containers/test/images/processes/instances/3001", subProcessLinks.get("1234"));
    }
}
