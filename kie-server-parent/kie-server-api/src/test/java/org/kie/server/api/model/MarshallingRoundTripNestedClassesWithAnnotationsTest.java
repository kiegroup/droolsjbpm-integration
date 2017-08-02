/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model;

import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.objects.NestedLevel1;
import org.kie.server.api.marshalling.objects.NestedLevel2;
import org.kie.server.api.marshalling.objects.NestedLevel3;
import org.kie.server.api.marshalling.objects.Top;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarshallingRoundTripNestedClassesWithAnnotationsTest {

    private static final Logger logger = LoggerFactory.getLogger(MarshallingRoundTripNestedClassesWithAnnotationsTest.class);

    private static Top createTestObject() {
        Top top = new Top();

        NestedLevel3 level3 = new NestedLevel3();
        level3.setName("Anton");

        NestedLevel2 level2 = new NestedLevel2();
        level2.setNestedLevel3(level3);

        NestedLevel1 level1 = new NestedLevel1();
        level1.setNestedLevel2(level2);

        top.setNestedLevel1(level1);

        return top;
    }

    private Set<Class<?>> getCustomClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Top.class);
        classes.add(NestedLevel3.class);
        classes.add(NestedLevel2.class);
        classes.add(NestedLevel1.class);
        return classes;
    }

    @Test
    public void testJaxb() {
        Marshaller marshaller = MarshallerFactory.getMarshaller( getCustomClasses(), MarshallingFormat.JAXB, getClass().getClassLoader() );
        verifyMarshallingRoundTrip( marshaller, createTestObject() );
    }

    @Test
    public void testXStream() {
        Marshaller marshaller = MarshallerFactory.getMarshaller( getCustomClasses(), MarshallingFormat.XSTREAM, getClass().getClassLoader() );
        verifyMarshallingRoundTrip( marshaller, createTestObject() );

    }

    @Test
    public void testJSON() {
        Marshaller marshaller = MarshallerFactory.getMarshaller( getCustomClasses(), MarshallingFormat.JSON, getClass().getClassLoader() );
        verifyMarshallingRoundTrip( marshaller, createTestObject() );
    }

    private void verifyMarshallingRoundTrip( Marshaller marshaller, Object inputObject ) {
        String rawContent = marshaller.marshall( inputObject );
        logger.info(rawContent);
        Object testObjectAfterMarshallingTurnAround = marshaller.unmarshall( rawContent, inputObject.getClass() );
        Assertions.assertThat( testObjectAfterMarshallingTurnAround ).isEqualTo( inputObject );
    }


}
