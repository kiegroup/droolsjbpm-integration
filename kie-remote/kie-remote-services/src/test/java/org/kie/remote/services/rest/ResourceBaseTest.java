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

package org.kie.remote.services.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.impl.model.xml.JaxbI18NText;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbString;

public class ResourceBaseTest extends ResourceBase {

    @Test
    public void statusEnumTest() {
       for( Status testStatus : Status.values())  {
          Status roundTripStatus = getEnum(testStatus.toString().toLowerCase());
          assertEquals( testStatus + " incorrectly processed!", testStatus, roundTripStatus);
       }
    }

    @Test
    public void testReadNumberAsString() {
        String numberAsString = "\"10\"";

        Map<String, String[]> params = new HashMap<String, String[]>();
        String [] data = {numberAsString};
        params.put("stringnumber", data);

        Object value = getObjectParam("stringnumber", true, params, "dummy");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("10", value);
    }

    @Test
    public void testReadNumberAsNumber() {
        String numberString = "10";

        Map<String, String[]> params = new HashMap<String, String[]>();
        String [] data = {numberString};
        params.put("number", data);

        Object value = getObjectParam("number", true, params, "dummy");
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(10L, value);
    }

    @Test
    public void testFloatRegex() {
        String regex = FLOAT_REGEX;
        String floot = String.valueOf(Float.MAX_VALUE);
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = String.valueOf(Float.MIN_NORMAL);
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = String.valueOf(Float.MIN_VALUE);
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = String.valueOf(new Float(103030303.0202030504502101f));
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));

        floot = "1.00f";
        float flootVal = Float.parseFloat(floot.substring(0, floot.length()-1));
        assertTrue( "Incorrect value: [" + floot + "]", flootVal > 0 );
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = "1034500f";
        flootVal = Float.parseFloat(floot.substring(0, floot.length()-1));
        assertTrue( "Incorrect value: [" + floot + "]", flootVal > 0 );
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = "1.00E32f";
        flootVal = Float.parseFloat(floot.substring(0, floot.length()-1));
        assertTrue( "Incorrect value: [" + floot + "]", flootVal > 0 );
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = "1.00E-3f";
        flootVal = Float.parseFloat(floot.substring(0, floot.length()-1));
        assertTrue( "Incorrect value: [" + floot + "]", flootVal > 0 );
        assertTrue( "[" + floot + "] | [" + regex + "]", floot.matches(regex));

        floot = ".00f"; // .\d
        assertFalse( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = "1.1234567891f"; // \d.\d{10,}
        assertFalse( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
        floot = "1.123E-293"; // E-\d{3}
        assertFalse( "[" + floot + "] | [" + regex + "]", floot.matches(regex));
    }

    @Test
    public void testWrapperLogic() {
        wrapperPrimitives.put(String.class, JaxbString.class);

        for( Class wrapperClass : wrapperPrimitives.values() ) {
            Constructor [] cntrs = wrapperClass.getConstructors();
            assertEquals( "Too many constructors for "+ wrapperClass.getSimpleName(),
                          2, cntrs.length);
            boolean noArgCntrFound = true;
            for( Constructor cntr : cntrs ) {
                if( cntr.getParameterTypes().length == 0 ) {
                    noArgCntrFound = true;
                }
                assertTrue( cntr.getParameterTypes().length < 2 );
            }
            assertTrue( "No-Arg constructor not found for "+ wrapperClass.getSimpleName(),
                        noArgCntrFound);
        }

        // should NOT be wrapped
        Object wrappee = new JaxbI18NText();
        Object wrapped = ResourceBase.wrapObjectIfNeeded(wrappee);
        assertTrue( wrappee.getClass().getSimpleName() + " was wrapped!",
                    wrappee == wrapped );

        // SHOULD be wrapped
        Map<String, Object> map = new HashMap<String, Object>(0);
        List<String> list = new ArrayList<String>(0);
        Set<String> set = new HashSet<String>(0);

        int [] intArr = { 1, 2, 3 };
        String [] strArr = { "a", "b", "c" };
        Float [] flArr = { new Float(1.01), new Float(1.001), new Float(1.0001) };

        Object [] wrappees = {
                true,
                new Byte("1").byteValue(),
                new Character('a').charValue(),
                new Double(23.01).doubleValue(),
                new Float(46.02).floatValue(),
                1011,
                1012,
                new Short("10").shortValue(),
                "string",
                list,
                set,
                map,
                intArr,
                strArr,
                flArr
        };

        for( Object input : wrappees ) {
            wrapped = ResourceBase.wrapObjectIfNeeded(input);
            assertFalse( input.getClass().getSimpleName() + " was not wrapped!",
                         input == wrapped );
        }
    }
}
