/*
 * Copyright 2017 - 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.xstream;

import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.SYSTEM_XSTREAM_ENABLED_PACKAGES;

import java.util.HashSet;

import org.jbpm.document.Document;
import org.jbpm.document.Documents;
import org.jbpm.document.service.impl.DocumentImpl;
import org.junit.After;
import org.junit.Test;
import org.kie.server.api.marshalling.xstream.KieServerTypePermission;

public class JBPMKieServerTypePermissionTest {

    @After
    public void cleanup() {
        System.clearProperty(SYSTEM_XSTREAM_ENABLED_PACKAGES);
    }
    
    @Test
    public void testDocumentReleatedClasses() {
        
        KieServerTypePermission permission = new KieServerTypePermission(new HashSet<>());
        
        assertTrue(permission.allows(Document.class));
        assertTrue(permission.allows(DocumentImpl.class));
        assertTrue(permission.allows(Documents.class));
    }   
}
