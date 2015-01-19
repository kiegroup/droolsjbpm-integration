package org.kie.remote.services.rest.query;

import static org.junit.Assert.assertFalse;
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

import org.junit.Ignore;
import org.junit.Test;
import org.kie.internal.query.QueryParameterIdentifiers;
import org.kie.remote.services.rest.ResourceBase;

public class QueryResourceDataTest extends QueryResourceData {

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
        Field [] fields = QueryParameterIdentifiers.class.getDeclaredFields();
        Map<Integer, String> idMap = new TreeMap<Integer, String>();
        for( Field field : fields ) { 
            Object objVal = field.get(null);
            if( ! (objVal instanceof String) ) { 
               continue; 
            }
            String val = (String) objVal;
            Integer idVal;
            try { 
               idVal = Integer.valueOf(val);
            } catch( Exception e ) { 
                continue;
            }
           idMap.put(idVal, field.getName());
        }
        
        for( Entry<Integer, String> entry : idMap.entrySet() ) { 
            int id = entry.getKey();
            String between = ( id < 10 ? " " : "") + " : ";
            System.out.println( id + between + entry.getValue());
        }
    }
    
}
