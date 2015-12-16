/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Test;

public class JaxRsAnnotationTest {

    @Test
    public void validateJaxRsAnnotations() { 
   
        Class [] resourceClasses = { 
                DeploymentResourceImpl.class,
                DeploymentsResourceImpl.class,
                ExecuteResourceImpl.class,
                HistoryResourceImpl.class,
                QueryResourceImpl.class,
                RuntimeResourceImpl.class,
                TaskResourceImpl.class };
        for( Class<?> resourceClass : resourceClasses ) { 
            validatePathAnnotations(resourceClass);
            validatePathParameterAnnotations(resourceClass);
        } 
    }
    
    private <T> void validatePathAnnotations(Class<T> resourceClass) { 
       Class<?>[] interfaces = resourceClass.getInterfaces(); 
       Path pathAnno = null;
       if( interfaces != null && interfaces.length > 0 ) { 
           assertEquals( "More than 1 interface for resource", 1, interfaces.length );
           Class<?> resInt = interfaces[0];
           pathAnno = resInt.getAnnotation(Path.class);
       } else { 
           pathAnno = resourceClass.getAnnotation(Path.class);
       }
       assertNotNull( resourceClass.getSimpleName() + " does not have a @Path annotation", pathAnno);
       List<String> pathParamIds = getPathParamIds(pathAnno.value());
      
       // each regex id in path expression should have a field
       Field [] fields = resourceClass.getDeclaredFields();
       Map<String, String> notInPathIds = new HashMap<String, String>();
       for( Field possibleParamField : fields ) { 
           possibleParamField.setAccessible(true);
           PathParam pathParamAnno = possibleParamField.getAnnotation(PathParam.class);
           if( pathParamAnno != null ) { 
               String pathParamFieldId = pathParamAnno.value();
               if( ! pathParamIds.remove(pathParamFieldId) ) { 
                  notInPathIds.put(pathParamFieldId, possibleParamField.getName()); 
               }
           }
       }
       if( ! pathParamIds.isEmpty() ) { 
          fail( resourceClass.getSimpleName() + " does not have a field for the @PathParam [" + pathParamIds.get(0) + "]" );
       }
       if( ! notInPathIds.isEmpty() ) { 
           Entry<String, String> entry = notInPathIds.entrySet().iterator().next();
           fail( "Path param id " + entry.getKey() + " on field " + resourceClass.getSimpleName() + "." + entry.getValue() + " not mentioned in path");
       }
               
    }
   
    private List<String> getPathParamIds(String path) { 
        Matcher m = Pattern.compile("[^\\{]+\\{(\\w+):[^\\}]+\\}").matcher(path);
        List<String> pathParamIds = new ArrayList<String>(m.groupCount());
        int start = 0;
        while( m.find(start)) { 
          String id = m.group(1);
          pathParamIds.add(id);
          start = m.end(1);
        } 
        return pathParamIds;
    }
    
    private void validatePathParameterAnnotations(Class resourceClass) { 
        Class<?>[] interfaces = resourceClass.getInterfaces(); 
        List<Method> methods = null;
        if( interfaces != null && interfaces.length > 0 ) { 
            assertEquals( "More than 1 interface for resource", 1, interfaces.length );
            Class<?> resInt = interfaces[0];
            methods = new ArrayList<Method>(Arrays.asList(resInt.getMethods()));
        } else { 
            methods = new ArrayList<Method>(Arrays.asList(resourceClass.getMethods()));
        }
        assertNotNull( resourceClass.getSimpleName() + " does not have any public methods!", methods);
        for( Method method : methods ) { 
           Path pathAnno = method.getAnnotation(Path.class);
           if( pathAnno == null ) { 
               continue;
           }
           List<String> pathParamIds = getPathParamIds(pathAnno.value());
           if( ! pathParamIds.isEmpty() ) { 
               Annotation [][] paramAnnotations = method.getParameterAnnotations();
               Set<String> argPathParams = new HashSet<String>();
               for( Annotation [] annos : paramAnnotations ) { 
                  for( Annotation anno : annos ) { 
                      if( anno instanceof PathParam ) { 
                         argPathParams.add(((PathParam) anno).value());
                      }
                  }
               }
               for( String pathParamId : pathParamIds ) { 
                   assertTrue( resourceClass.getSimpleName() + "." + method.getName() +
                           " parameters do not reference a PathParam id [" + pathParamId + "]",
                           argPathParams.remove(pathParamId));
               }
               if( ! argPathParams.isEmpty() ) { 
                  fail( resourceClass.getSimpleName() + "." + method.getName() +
                          " @Path annotation does not mention PathParam annotation id [" + argPathParams.iterator().next() + "]" );
               }
           }
        }
        
        
    }
}
