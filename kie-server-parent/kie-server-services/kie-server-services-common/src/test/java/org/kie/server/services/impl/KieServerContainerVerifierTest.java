/**
 *  Copyright 2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.kie.server.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class KieServerContainerVerifierTest {

    @Test
    public void testMainEmpty() throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        boolean verified = KieServerContainerVerifier.main(new String[]{}, null, new PrintStream(err, true));
        assertTrue(verified);
        assertEquals(KieServerContainerVerifier.USAGE, new String(err.toByteArray(), "UTF-8"));
    }

}
