/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.runtime.pipeline.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.builder.impl.KnowledgeBuilderImpl;
import org.drools.runtime.pipeline.Callable;
import org.drools.runtime.pipeline.ListAdapter;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.BasePipelineContext;
import org.drools.runtime.pipeline.impl.JxlsTransformer;
import org.drools.runtime.pipeline.impl.ListAdapterImpl;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.ResourceType;
import org.kie.io.ResourceFactory;

import static org.junit.Assert.*;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSDataReadException;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;

public class JxlsTest {
    @Test
    public void test1() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        // kbuilder.add( ResourceFactory.newClassPathResource( "test_JXLS.drl", getClass() ), ResourceType.DRL );
        
        ClassLoader cl = ((KnowledgeBuilderImpl)kbuilder).getPackageBuilder().getRootClassLoader();
        Thread.currentThread().setContextClassLoader( cl );

        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream( "departmentData.xls"));
        
        XLSReader mainReader = ReaderBuilder.buildFromXML( ResourceFactory.newClassPathResource( "departments.xml", getClass() ).getInputStream() );
        Transformer transformer = PipelineFactory.newJxlsTransformer(mainReader, "[ 'departments' : new java.util.ArrayList(), 'company' : new org.drools.runtime.pipeline.impl.Company() ]");
        
        Callable callable = new CallableImpl();

        callable.setReceiver( transformer );
        transformer.setReceiver( callable );
        BasePipelineContext context = new BasePipelineContext( Thread.currentThread().getContextClassLoader() );
        
        Map<String, Object> beans = ( Map<String, Object> ) callable.call( inputXLS, context );

        assertEquals( Company.class.getName(), beans.get( "company" ).getClass().getName());
        assertEquals( ArrayList.class.getName(), beans.get( "departments" ).getClass().getName());
        
        Company company = ( Company )  beans.get( "company" );
        assertEquals( "A-Team", company.getName() );
        assertEquals( 4, company.getEmployee().size() );
        
        List<Department> departments = ( List<Department> ) beans.get( "departments" );
        assertEquals( 3, departments.size() );
        
        Department department = departments.get( 0 );
        assertEquals( "IT", department.getName() );
        assertEquals( 5, department.getStaff().size() );
        
        department = departments.get( 2 );
        assertEquals( "BA", department.getName() );
        assertEquals( 4, department.getStaff().size() );
    }
    
}
