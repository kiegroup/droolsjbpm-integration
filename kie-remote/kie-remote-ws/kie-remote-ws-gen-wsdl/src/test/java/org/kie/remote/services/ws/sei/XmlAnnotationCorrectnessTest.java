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

package org.kie.remote.services.ws.sei;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class XmlAnnotationCorrectnessTest {

    private static Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.remote.services.sei"),
            new TypeAnnotationsScanner(), new SubTypesScanner());

    @Test
    public void testCorrectNames() {
        Set<Class<?>> xmlTypeClasses = reflections.getTypesAnnotatedWith(XmlType.class);
        for( Class<?> xmlTypeClass : xmlTypeClasses ) {
            if( !xmlTypeClass.getPackage().getName().startsWith("org.kie.remote.services.sei") ) {
                continue;
            }
            String className = xmlTypeClass.getSimpleName();
            XmlType xmlTypeAnno = xmlTypeClass.getAnnotation(XmlType.class);
            assertEquals("Name property inequal for " + className, className, xmlTypeAnno.name());
            Set<String> properties = new HashSet<String>(Arrays.asList(xmlTypeAnno.propOrder()));
            Set<String> fields = new HashSet<String>();
            for( Field field : xmlTypeClass.getFields() ) {
                String fieldName = field.getName();
                assertTrue(fieldName + " not present in XmlType propOrder value in " + className, properties.contains(fieldName));
                fields.add(fieldName);
            }
            for( String prop : properties ) {
                assertTrue(prop + " XmlType propOrder values, but not a field in " + className, fields.contains(prop));
            }
        }
    }
}
