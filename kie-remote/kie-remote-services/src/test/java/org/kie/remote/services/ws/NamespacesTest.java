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

package org.kie.remote.services.ws;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.*;

import javax.jws.WebService;

import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

@SuppressWarnings("unchecked")
public class NamespacesTest {

    Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.remote.services.ws"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner());
    
    @Test
    @Ignore
    public void nameSpacesAreCoorrectTest() throws Exception { 
        Set<Class<?>> webServiceImplClasses = reflections.getTypesAnnotatedWith(WebService.class);
        assertTrue( "No classes found!", webServiceImplClasses.size() > 0 );

        for( Class wsCl : webServiceImplClasses ) { 
            if( wsCl.getSimpleName().endsWith("Impl") ) { 
               Field nsField = wsCl.getDeclaredField("NAMESPACE");
               nsField.setAccessible(true);
               String implNamespace = (String) nsField.get(null);
               String defNamespace = ((WebService) wsCl.getAnnotation(WebService.class)).targetNamespace();
               assertEquals(wsCl.getSimpleName() + " namespace is incorrectly defined in the impl class", 
                       defNamespace, implNamespace);
            } else if( wsCl.getSimpleName().endsWith("WebServce") ) {
                fail( "Unexpected name for a webservice interface: " + wsCl.getName());
            }
        }
    }
}
