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

package org.kie.server.api.marshalling.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;

import static org.junit.Assert.*;

public class JSONMarshallerExtensionTest {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Test
    public void testCustomExtensionMarshaller() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        Calendar calendar = GregorianCalendar.getInstance();
        
        String marshall = marshaller.marshall(calendar);
        assertEquals(marshall, "\""+ FORMATTER.format(calendar.getTime()) +"\"" );
        
        GregorianCalendar unmarshall = marshaller.unmarshall(marshall, GregorianCalendar.class);
        assertEquals(unmarshall, calendar);
    }
    
}
