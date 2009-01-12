package org.drools.runtime.pipeline.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.impl.KnowledgeBuilderImpl;
import org.drools.io.ResourceFactory;
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

import junit.framework.TestCase;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSDataReadException;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;

public class JxlsTest extends TestCase {
    public void test1() throws Exception {
        InputStream stream = getClass().getResourceAsStream( "departments.xml");
        assertNotNull( stream );
        
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        // kbuilder.add( ResourceFactory.newClassPathResource( "test_JXLS.drl", getClass() ), ResourceType.DRL );
        
        ClassLoader cl = ((KnowledgeBuilderImpl)kbuilder).pkgBuilder.getRootClassLoader(); 
        Thread.currentThread().setContextClassLoader( cl );
        
        InputStream inputXML = new BufferedInputStream( stream );
        XLSReader mainReader = ReaderBuilder.buildFromXML( inputXML );
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream( "departmentData.xls"));
        
        Callable callable = new CallableImpl();
        Transformer transformer = PipelineFactory.newJxlsTransformer(mainReader, "[ 'departments' : new java.util.ArrayList(), 'company' : new org.drools.runtime.pipeline.impl.Company() ]");
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