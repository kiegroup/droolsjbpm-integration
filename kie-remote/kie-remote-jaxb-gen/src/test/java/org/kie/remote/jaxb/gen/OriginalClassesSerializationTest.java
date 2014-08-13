package org.kie.remote.jaxb.gen;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class OriginalClassesSerializationTest {

    private static Reflections reflections = new Reflections(
            ClasspathHelper.forPackage("org.drools.command"),
            ClasspathHelper.forPackage("org.jbpm.services.task.commands"),
            ClasspathHelper.forPackage("org.jbpm.process.audit.command"),
            new TypeAnnotationsScanner(), 
            new FieldAnnotationsScanner(), 
            new SubTypesScanner());
    
    @Test
    public void acceptedCommandsTest() throws Exception {
      
        int i = 0;
        for (Class<?> cmdClass : reflections.getTypesAnnotatedWith(XmlAccessorType.class) ) {
            if( cmdClass.getAnnotation(XmlEnum.class) != null ) { 
                continue;
            }
            ++i;
            try {
                cmdClass.getConstructor(new Class[0]);
            } catch (Exception e) {
                fail("Class " + cmdClass.getSimpleName() + " does not have a no-arg constructor.");
            }
        }
        assertTrue( "No classes checked! [" + i + "]", i > 30 );
    }
  
}
