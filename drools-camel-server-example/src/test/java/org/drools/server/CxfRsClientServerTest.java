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

package org.drools.server;

import static org.junit.Assert.assertTrue;

import org.apache.camel.CamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CxfRsClientServerTest {

    @org.junit.Test
    public void test1() throws Exception {
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext( "classpath:beans-test.xml" );

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"message\">\n";
        cmd += "      <org.drools.server.Message>\n";
        cmd += "         <text>Hello World</text>\n";
        cmd += "      </org.drools.server.Message>\n";
        cmd += "   </insert>\n";
        cmd += "</batch-execution>\n";

        CamelServerApp test = new CamelServerApp();
        String response = test.execute( cmd,
                                        (CamelContext) springContext.getBean( "camel-client-ctx" ) );

        assertTrue( response.contains( "execution-results" ) );
        assertTrue( response.contains( "echo" ) );
        springContext.stop();
    }

}
