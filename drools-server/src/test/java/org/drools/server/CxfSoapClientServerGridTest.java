/**
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

package org.drools.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;

import org.apache.camel.CamelContext;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.ConnectionFactoryService;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.SocketService;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.io.impl.ByteArrayResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Assert;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CxfSoapClientServerGridTest extends TestCase {

    public void test1() throws Exception {
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext( "classpath:beans-test-grid.xml" );

        SOAPMessage soapMessage = createMessageForKsession("ksession3");

        Test test = new Test();
        String response = test.execute( soapMessage,
                                        (CamelContext) springContext.getBean( "camel-client-ctx" ) );
        
        //System.out.println("Response 1 = "+ response ); 
        assertTrue( response.contains( "execution-results" ) );
        assertTrue( response.contains( "echo" ) );
        
        
        GridImpl grid2 = (GridImpl) springContext.getBean("grid2");
        GridServiceDescription<GridNode> n1Gsd = grid2.get( WhitePages.class ).lookup( "node1" );
        GridConnection<GridNode> conn = grid2.get( ConnectionFactoryService.class ).createConnection( n1Gsd );
        GridNode remoteN1 = conn.connect();

        KnowledgeBuilder kbuilder = remoteN1.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        Assert.assertNotNull( kbuilder );
        
        String rule = "package  org.grid.test\n"
                + "declare Message2\n"
                +    "text : String\n"
                + "end\n"
                + "rule \"echo2\" \n"
                + "dialect \"mvel\"\n"
                +   "when\n"
                + "     $m : Message2()\n"
                +   "then\n"
                +       "$m.text = \"echo2:\" + $m.text;\n"
                + "end\n";
                //System.out.println("Rule = "+rule);
        kbuilder.add( new ByteArrayResource( rule.getBytes() ),
                      ResourceType.DRL );

        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( errors != null && errors.size() > 0 ) {
            for ( KnowledgeBuilderError error : errors ) {
                System.out.println( "Error: " + error.getMessage() );

            }
            fail("KnowledgeBase did not build");
        }

        KnowledgeBase kbase = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();

        Assert.assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        Assert.assertNotNull( session );
        
        
        remoteN1.set("ksession2", session);
        
        
        soapMessage = createMessageForKsession("ksession2");
        
        String response2 = test.execute( soapMessage,
                                        (CamelContext) springContext.getBean( "camel-client-ctx" ) );
        
        //System.out.println("Response 2 = "+response2  );
        assertTrue( response2.contains( "execution-results" ) );
        assertTrue( response2.contains( "echo2" ) );
        
        remoteN1.dispose();
        GridImpl grid1 = (GridImpl) springContext.getBean("grid1");
        grid1.get(SocketService.class).close();
       
        springContext.registerShutdownHook();
        springContext.stop();
        
    }
    
    private SOAPMessage createMessageForKsession(String ksessionName) throws SOAPException{
        
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        QName payloadName = new QName( "http://soap.jax.drools.org",
                                       "execute",
                                       "ns1" );

        body.addBodyElement( payloadName );
        String add="";
        String packages = "org.test";
        if(ksessionName.equals("ksession2")){
            add="2";
            packages = "org.grid.test";
        }
        String cmd = "";
        cmd += "<batch-execution lookup=\""+ksessionName+"\">\n";
        cmd += "  <insert out-identifier=\"message\">\n";
        cmd += "      <"+packages+".Message"+add+">\n";
        cmd += "         <text>Helllo World"+ksessionName+"</text>\n";
        cmd += "      </"+packages+".Message"+add+">\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";
        
        body.addTextNode( cmd );
        OutputStream os = new ByteArrayOutputStream();
        try {
            soapMessage.writeTo(os);
        } catch (IOException ex) {
            Logger.getLogger(CxfSoapClientServerGridTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("SOAP = "+os.toString());
        return soapMessage;
    
    }

}
