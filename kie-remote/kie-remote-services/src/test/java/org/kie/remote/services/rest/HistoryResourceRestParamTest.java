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

package org.kie.remote.services.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.junit.Test;

public class HistoryResourceRestParamTest {

    @Test
    public void getVariableInstanceLogsByVariableIdByVariableValueTest() throws NoSuchMethodException, SecurityException {

        Path pathAnno = HistoryResourceImpl.class.getMethod( "getVariableInstanceLogsByVariableIdByVariableValue",
                                                             String.class,
                                                             String.class ).getAnnotation( Path.class );
        String path = pathAnno.value();
        path = path.replace( "/variable/{varId: [a-zA-Z0-9-:\\._]+}/value/{value: ",
                             "" );
        String regex = path.substring( 0,
                                       path.length() - 1 );
        // Test : value with space
        String test = "my%20value%20with%20spaces";
        assertTrue( test,
                    Pattern.matches( regex,
                                     test ) );
        test = "my value";
        assertTrue(test,
                Pattern.matches(regex,
                        test));
    }

    @Test
    public void getProcessInstanceLogsByVariableIdByVariableValueTest() throws NoSuchMethodException, SecurityException {

        Path pathAnno = HistoryResourceImpl.class.getMethod( "getProcessInstanceLogsByVariableIdByVariableValue",
                                                             String.class,
                                                             String.class ).getAnnotation( Path.class );
        String path = pathAnno.value();
        path = path.replace( "/variable/{varId: [a-zA-Z0-9-:\\._]+}/value/{value: ",
                             "" );
        String regex = path.substring( 0,
                                       path.indexOf( "}" ) );
        // Test : value with space
        String test = "my%20value%20with%20spaces";
        assertTrue( test,
                    Pattern.matches( regex,
                                     test ) );
        test = "my value";
        assertTrue(test,
                Pattern.matches(regex,
                        test));

    }

}
