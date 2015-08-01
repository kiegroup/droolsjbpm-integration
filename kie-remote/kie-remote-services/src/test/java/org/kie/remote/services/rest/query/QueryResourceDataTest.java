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

import static org.jbpm.query.jpa.data.QueryParameterIdentifiersUtil.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.kie.remote.services.rest.ResourceBase.PROC_INST_ID_PARAM_NAME;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.internal.query.QueryParameterIdentifiers;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.query.data.QueryResourceData;

/**
 * This tests minor methods and logic related to the REST Query operation.
 */
public class QueryResourceDataTest extends QueryResourceData {

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
    
    @Test
    public void testUniqueParameters() throws Exception { 
    
        List<Field> paramFields = 
                new LinkedList<Field>(Arrays.asList(QueryResourceData.class.getDeclaredFields()));
        Iterator<Field> iter = paramFields.iterator();
        List<String[]> allParams = new ArrayList<String[]>(paramFields.size());
        while( iter.hasNext() ) { 
           Field field = iter.next();
           if( field.getName().equals("minMaxParams")
               || field.getName().equals("nameValueParams") ) { 
               continue;
           }
           if( ! Modifier.isStatic(field.getModifiers()) ) { 
              iter.remove();
              continue;
           }
           if( ! field.getType().equals(String[].class) ) { 
              continue; 
           }
           if( ! field.getName().contains("Params") ) { 
              continue;
           }
           field.setAccessible(true);
           allParams.add((String []) field.get(null));
        }
    
        assertFalse( "No params found", allParams.isEmpty() ); 
        
        Set<String> params = new HashSet<String>();
        for( String [] paramArr : allParams ) { 
            for( String param : paramArr ) { 
                if( param == null ) { 
                    continue;
                }
                assertTrue( "Param \"" + param + "\" contains uppercase letters", 
                        param.toLowerCase().equals(param) );
                assertTrue( "Param \"" + param + "\" is used twice", 
                        params.add(param) );
            }
        }

        for( String param : ResourceBase.paginationParams ){ 
            assertTrue( "Param \"" + param + "\" contains uppercase letters", 
                    param.toLowerCase().equals(param) );
            assertTrue( "Param \"" + param + "\" is used twice", 
                    params.add(param) );
        }
        assertTrue( "Param \"" +  PROC_INST_ID_PARAM_NAME + "\" is used twice", 
                params.add(PROC_INST_ID_PARAM_NAME) );
    }
    
    @Test
    @Ignore
    // "test" for printing/checking the switch logic in the QueryResourceImpl class
    public void debugPrintSwitch() throws Exception { 
        TreeMap<Integer, String> sortedActionParamMap = new TreeMap<Integer, String>(actionParamNameMap);
       for( Entry<Integer, String> action : sortedActionParamMap.entrySet() ) {
           int num = action.getKey();
           switch( num ) { 
           case 0: 
               System.out.println( "\n// general");
               break;
           case GENERAL_END: 
               System.out.println( "\n// task");
               break;
           case TASK_END: 
               System.out.println( "\n// process instance");
               break;
           case PROCESS_END: 
               System.out.println( "\n// variable instance");
               break;
           case VARIABLE_END: 
               System.out.println( "\n// meta");
               break;
           }
           System.out.println( "case " + num + ": // " + action.getValue()  + "\nbreak;");
       }
    }
    
       
    @Test
    @Ignore
    public void debugPrintQueryParameterIds() throws Exception { 
        Map<Integer, String> idMap = getQueryParameterIdNameMap();
        for( Entry<Integer, String> entry : idMap.entrySet() ) { 
            int id = entry.getKey();
            String between = ( id < 10 ? " " : "") + " : ";
            System.out.println( id + between + entry.getValue());
        }
    }
    

    
}
