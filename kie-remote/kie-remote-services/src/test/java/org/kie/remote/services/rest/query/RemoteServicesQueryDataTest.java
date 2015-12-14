/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.rest.query;

import static org.jbpm.query.jpa.data.QueryParameterIdentifiersUtil.getQueryParameterIdNameMap;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;

/**
 * This tests minor methods and logic related to the REST Query operation.
 */
public class RemoteServicesQueryDataTest {

    @Test
    public void testUniqueQueryParameterIdentifiersBeingUsed() throws Exception {
        JbpmJUnitBaseTestCase test = new JbpmJUnitBaseTestCase(true, true, "org.jbpm.domain") { };
        test.setUp();
        boolean initialized = false;
        try {
            initialized = RemoteServicesQueryData.initializeCriteriaAttributes();
        } catch( Throwable t ) {
           String msg = t.getMessage();
           int length = "List Id [".length();
           String idStr = msg.substring(length, msg.indexOf(']'));
           int id = Integer.parseInt(idStr);
           String name = getQueryParameterIdNameMap().get(id);
           msg = msg.substring(0,length) + name + msg.substring(length+idStr.length());
           fail(msg);
        }
        assertTrue( "Criteria attributes was not initialized!", initialized);
        test.tearDown();
    }

}
