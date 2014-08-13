package org.kie.remote.services.ws;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.*;

import javax.jws.WebService;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class NamespacesTest {

    Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.remote.services.ws"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner());
    
    @Test
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
