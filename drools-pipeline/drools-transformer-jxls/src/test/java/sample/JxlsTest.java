package sample;

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
        
        List list = new ArrayList();
        Callable callable = PipelineFactory.newCallable();
        Transformer transformer = PipelineFactory.newJxlsTransformer(mainReader, "[ 'departments' : new java.util.ArrayList(), 'company' : new sample.Company() ]");
        callable.addReceiver( transformer );
        transformer.addReceiver( callable );
//        ListAdapter adapter = PipelineFactory.newListAdapter( list, true );
//        transformer.addReceiver( adapter );
        
        BasePipelineContext context = new BasePipelineContext( Thread.currentThread().getContextClassLoader() );
        
        Map<String, Object> beans = ( Map<String, Object> ) callable.call( inputXLS, context );
        
        //callable.signal( inputXLS, context );        
        //Map<String, Object> beans = ( Map<String, Object> ) list.get( 0 );

        System.out.println( beans.get( "company" ) );
        System.out.println( beans.get( "departments" ) );        
    }
    
}