package org.drools.camel.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.pipeline.camel.Person;
import org.drools.runtime.pipeline.impl.DroolsJaxbHelperProviderImpl;

public class JaxbTest extends TestCase {
    public void test1() throws Exception {
        List<String> classesName = new ArrayList();     
//        classesName.add("org.drools.model.AddressType");
//        classesName.add("org.drools.model.ObjectFactory");
//        classesName.add("org.drools.model.Person");
        classesName.add("org.drools.pipeline.camel.Person");
        
        //jaxbDataFormat = new JaxbDataFormat();
        //jaxbDataFormat.setContextPath( contextPath )
        Set<String> set = new HashSet<String>();
        for ( String clsName : DroolsJaxbHelperProviderImpl.JAXB_ANNOTATED_CMD ) {
            set.add( clsName.substring( 0, clsName.lastIndexOf( '.' ) ) );
        }
        
        for ( String clsName : classesName) {
            set.add( clsName.substring( 0, clsName.lastIndexOf( '.' ) ) );
        }       
        
        StringBuilder sb = new StringBuilder();
        for ( String pkgName : set ) {
            sb.append( pkgName );
            sb.append( ':' );
        }
        
        System.out.println( "context path: " + sb.toString() );
//        jaxbDataFormat.setContextPath( sb.toString() );
//        jaxbDataFormat.setPrettyPrint( true ); 
        JAXBContext jaxbContext = JAXBContext.newInstance(sb.toString());
        
        List<GenericCommand<?>> cmds = new ArrayList<GenericCommand<?>>();
        cmds.add( new InsertObjectCommand( new Person("darth", 21), "p" ) );
        cmds.add( new GetGlobalCommand( "xxx" ) );
        cmds.add( new SetGlobalCommand( "yyy", new Person("yoda", 21) ));
        BatchExecutionCommand batch = new BatchExecutionCommand(cmds);
        
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal( batch, baos );
        
        System.out.println( baos );
        
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        BatchExecutionCommand batch2 = ( BatchExecutionCommand ) unmarshaller.unmarshal( new ByteArrayInputStream( baos.toByteArray() ) );
        baos = new ByteArrayOutputStream();
        marshaller.marshal( batch2, baos );
        System.out.println( baos );
    }
}
