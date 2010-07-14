package org.drools.camel.component.cxf;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.camel.test.CamelSpringTestSupport;
import org.drools.core.util.StringUtils;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CxfTest2 extends CamelSpringTestSupport {
    
    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {        
        return new ClassPathXmlApplicationContext("org/drools/camel/component/CxfRsSpring.xml");
    }    
    
    public void test1() throws Exception {
//        String cmd = "";
//        cmd += "<batch-execution lookup=\"ksession1\">\n";
//        cmd += "  <insert out-identifier=\"salaboy\">\n";
//        cmd += "      <org.drools.pipeline.camel.Person>\n";
//        cmd += "         <name>salaboy</name>\n";
//        cmd += "      </org.drools.pipeline.camel.Person>\n";
//        cmd += "   </insert>\n";
//        cmd += "   <fire-all-rules/>\n";
//        cmd += "</batch-execution>\n";
//        
//        
//        Object object = this.context.createProducerTemplate().requestBody("direct://http", cmd);
//        System.out.println( object ); 
    }
    
}
