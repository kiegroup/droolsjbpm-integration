package org.drools.server;

import org.apache.camel.CamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class CxfRsClientServerTest extends TestCase {
      
    
    public void test1() throws Exception {
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath:beans-test.xml");
        
        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"message\">\n";
        cmd += "      <org.test.Message>\n";
        cmd += "         <text>Helllo World</text>\n";
        cmd += "      </org.test.Message>\n";
        cmd += "   </insert>\n";
        cmd += "</batch-execution>\n";
               
        Test test = new Test();
        String response = test.execute( cmd,
                                        ( CamelContext ) springContext.getBean( "camel-client-ctx" ) );
        
        assertTrue( response.contains( "execution-results" ) );
        assertTrue( response.contains( "echo" ) ); 
    }
    
}
