/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.util.HashSet;

import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;

import static org.junit.Assert.assertEquals;

public class JSONMarshallerWithSystemPropertyTest {

    @BeforeClass
    public static void init() {
        System.setProperty(KieServerConstants.KIE_SERVER_STRICT_JAVABEANS_SERIALIZERS, "true");
    }

    @AfterClass
    public static void tearDown () {
        System.clearProperty(KieServerConstants.KIE_SERVER_STRICT_JAVABEANS_SERIALIZERS);
    }
    
    @Test
    public void testCapitalizedWrapObjectFieldnamesWithProperty() throws Exception {

        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<>(), MarshallingFormat.JSON, getClass().getClassLoader());

        BatchExecutionCommandImpl batch = new BatchExecutionCommandImpl();
        batch.addCommand(new InsertObjectCommand(new Order("all")));

        
        String converted = marshaller.marshall(batch);
        assertEquals("{\n"
                + "  \"lookup\" : null,\n"
                + "  \"commands\" : [ {\n"
                + "    \"insert\" : {\n"
                + "      \"object\" : {\"org.kie.server.api.marshalling.JSONMarshallerWithSystemPropertyTest$Order\":{\n"
                + "  \"ORDER_ID\" : \"all\"\n"
                + "}},\n"
                + "      \"out-identifier\" : null,\n"
                + "      \"return-object\" : true,\n"
                + "      \"entry-point\" : \"DEFAULT\",\n"
                + "      \"disconnected\" : false\n"
                + "    }\n"
                + "  } ]\n"
                + "}", converted);
        BatchExecutionCommandImpl unconverted = marshaller.unmarshall(converted, BatchExecutionCommandImpl.class);
        assertEquals("all", ((Order) ((InsertObjectCommand) unconverted.getCommands().get(0)).getObject()).getORDER_ID());
    }
    
    public static class Order {

        private String ORDER_ID;
        
        public Order() {}
        
        public Order(String o){
            this.ORDER_ID = o;
        }

        public String getORDER_ID() {
            return ORDER_ID;
        }

        public void setORDER_ID(String o) {
            this.ORDER_ID = o;
        }
    }

}
