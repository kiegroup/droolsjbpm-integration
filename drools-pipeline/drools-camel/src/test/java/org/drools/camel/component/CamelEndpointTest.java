/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.camel.component;

import java.util.Collection;

import javax.naming.Context;

import org.apache.camel.CamelException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.vsm.ServiceManager;
import org.drools.vsm.local.ServiceManagerLocalClient;

public class CamelEndpointTest extends ContextTestSupport {
    private ServiceManager sm;

    public void testBasic() throws Exception {
        String inXml = "";
        inXml += "<batch-execution lookup=\"ksession1\">";
        inXml += "  <insert out-identifier='salaboy'>";
        inXml += "    <org.drools.pipeline.camel.Person>";
        inXml += "      <name>salaboy</name>";
        inXml += "    </org.drools.pipeline.camel.Person>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        Object response = template.requestBody("direct:in", inXml);
        
        // Urgh, ugly stuff, but it's getting late...
        System.out.println(response);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:in").to("drools:sm/ksession1?method=execute");
            }
        };
    }
    
    @Override
    protected Context createJndiContext() throws Exception {
        Context context = super.createJndiContext();

        String rule = "";
        rule += "package org.drools.pipeline.camel;\n" +
                "import org.drools.pipeline.camel.Person;\n" +
                "rule 'Check for Person'\n" +
                " when\n" +
                "   $p: Person()\n" +
                " then\n" +
                "   System.out.println(\"Person Name: \" + $p.getName());\n" +
                "end\n";

        sm = new ServiceManagerLocalClient();
        StatefulKnowledgeSession ksession = getVmsSessionStateful(sm, rule);
        sm.register("ksession1", ksession);

        context.bind("sm", sm);
        return context;
    }
    
    private StatefulKnowledgeSession getVmsSessionStateful(ServiceManager sm, String rule) throws Exception {
        KnowledgeBuilder kbuilder = sm.getKnowledgeBuilderFactory().newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(rule.getBytes()), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            throw new CamelException(kbuilder.getErrors().toString());
        }

        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();
        KnowledgeBase kbase = sm.getKnowledgeBaseFactory().newKnowledgeBase();
        kbase.addKnowledgePackages(pkgs);
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        return session;
    }
}
