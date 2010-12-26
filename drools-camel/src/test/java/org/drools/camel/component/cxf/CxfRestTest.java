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

package org.drools.camel.component.cxf;

import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CxfRestTest extends CamelSpringTestSupport {

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext( "org/drools/camel/component/CxfRsSpring.xml" );
    }

    @Test
    public void test1() throws Exception {
        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"salaboy\">\n";
        cmd += "      <org.drools.pipeline.camel.Person>\n";
        cmd += "         <name>salaboy</name>\n";
        cmd += "      </org.drools.pipeline.camel.Person>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        Object object = this.context.createProducerTemplate().requestBody( "direct://http",
                                                                           cmd );
        System.out.println( object );
    }

}
