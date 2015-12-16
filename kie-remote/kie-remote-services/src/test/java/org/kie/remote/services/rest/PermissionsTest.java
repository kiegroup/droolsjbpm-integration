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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.junit.Test;
import org.kie.internal.remote.PermissionConstants;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class PermissionsTest {

    
    private static Reflections reflections = new Reflections(
            ClasspathHelper.forPackage("org.kie.remote.services.rest"),
            new TypeAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

    @Test
    public void allRestMethodsHaveRolesAssigned() { 
       Set<Method> restMethods = reflections.getMethodsAnnotatedWith(Path.class);
       restMethods.addAll(reflections.getMethodsAnnotatedWith(GET.class));
       restMethods.addAll(reflections.getMethodsAnnotatedWith(POST.class));
       restMethods.addAll(reflections.getMethodsAnnotatedWith(DELETE.class));
       restMethods.addAll(reflections.getMethodsAnnotatedWith(PUT.class));
       
       for( Method pathMethod : restMethods ) { 
          RolesAllowed rolesAllowedAnno = pathMethod.getAnnotation(RolesAllowed.class);
          assertNotNull( pathMethod.getDeclaringClass() + "." +  pathMethod.getName() + "(...) is missing a @RolesAllowed annotation!", 
                  rolesAllowedAnno);
          
          if( pathMethod.getDeclaringClass().equals(ExecuteResourceImpl.class) ) { 
              continue;
          }
          boolean basicRestRoleFound = false;
          for( String role : rolesAllowedAnno.value() ) { 
            if( PermissionConstants.REST_ROLE.equals(role) ) { 
                basicRestRoleFound = true;
                break;
            }
          }
          assertTrue( pathMethod.getDeclaringClass() + "." +  pathMethod.getName() + "(...) is does not have the " + PermissionConstants.REST_ROLE + " role",
                  basicRestRoleFound);
       }
    }
    
    @Test
    public void readOnlyRolesProperlyAssigned() { 
       Set<Method> restMethods = reflections.getMethodsAnnotatedWith(Path.class);
       restMethods.addAll(reflections.getMethodsAnnotatedWith(GET.class));
       restMethods.addAll(reflections.getMethodsAnnotatedWith(POST.class));
       restMethods.addAll(reflections.getMethodsAnnotatedWith(DELETE.class));
       restMethods.addAll(reflections.getMethodsAnnotatedWith(PUT.class));
       
       for( Method pathMethod : restMethods ) { 
          RolesAllowed rolesAllowedAnno = pathMethod.getAnnotation(RolesAllowed.class);
          assertNotNull( pathMethod.getDeclaringClass() + "." +  pathMethod.getName() + "(...) is missing a @RolesAllowed annotation!", 
                  rolesAllowedAnno);

          String [] roles =  rolesAllowedAnno.value(); 
          boolean readOnlyRoleFound = false;
          for( String role : roles ) { 
            if( PermissionConstants.REST_PROCESS_RO_ROLE.equals(role)
                    || PermissionConstants.REST_TASK_RO_ROLE.equals(role) ) { 
                readOnlyRoleFound = true;
                break;
            }
          }
          if( readOnlyRoleFound ) {
              assertNotNull( pathMethod.getDeclaringClass() + "." +  pathMethod.getName() + "(...) is read-only but not a @GET method!",
                      pathMethod.getAnnotation(GET.class));
          } else { 
              if( "query".equals(pathMethod.getName()) ) { // PermissionConstants.REST_QUERY_ROLE
                  
                  continue;
              }
              Class methodClass = pathMethod.getDeclaringClass();
              if( RuntimeResourceImpl.class.equals(methodClass) || TaskResourceImpl.class.equals(methodClass) || HistoryResourceImpl.class.equals(methodClass) ) { 
              assertNull( pathMethod.getDeclaringClass() + "." +  pathMethod.getName() + "(...) is read-only but not a @GET method!",
                      pathMethod.getAnnotation(GET.class));
                  
              }
          }
       }
    }
}
