/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.drools.camel.component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.naming.Context;
import junit.framework.TestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.util.jndi.JndiContext;
import org.drools.KnowledgeBase;
import org.drools.Person;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.command.BatchExecutionCommand;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridImpl;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.junit.Before;
import org.junit.Test;

/**
 * Camel - JAXB reproducer test
 * https://bugzilla.redhat.com/show_bug.cgi?id=771203
 * https://bugzilla.redhat.com/show_bug.cgi?id=771209
 * @author rsynek
 */
public class JaxbInsertTest extends TestCase {
    private ProducerTemplate template;       
    private StatefulKnowledgeSession ksession;
    
    @Before
    public void setUp() throws Exception {
        ksession = getKbase().newStatefulKnowledgeSession();
        initializeTemplate(ksession);
    }
    
    /**
     * configures camel-drools integration and defines 3 routes:
     * 1) testing route (connection to drools with JAXB command format)
     * 2) unmarshalling route (for unmarshalling command results)
     * 3) marshalling route (enables creating commands through API and converting to XML)
     */
    private CamelContext configure(StatefulKnowledgeSession session) throws Exception {

        GridImpl grid = new GridImpl(new HashMap<String, Object>());        
	GridNode node = grid.createGridNode("testnode");
        
	Context context = new JndiContext();
	context.bind("testnode", node);
        node.set("ksession", session);
        
        CamelContext camelContext = new DefaultCamelContext(context);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {          
                JaxbDataFormat jdf = new JaxbDataFormat();
                jdf.setContextPath("org.drools");
                jdf.setPrettyPrint(true);

                from("direct:test-session").policy(new DroolsPolicy()).unmarshal(jdf).to("drools://testnode/ksession").marshal(jdf);
                from("direct:unmarshall").policy(new DroolsPolicy()).unmarshal(jdf);
                from("direct:marshall").policy(new DroolsPolicy()).marshal(jdf);
            }
        });
        
        return camelContext;
    } 
    
    /**
     * camel context startup and template creation
     */
    private void initializeTemplate(StatefulKnowledgeSession session) throws Exception {
        CamelContext context = configure(session);
        this.template = context.createProducerTemplate();
        context.start();
    }

    /**
     * creates empty knowledge base
     */
    private KnowledgeBase getKbase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        return kbuilder.newKnowledgeBase();
    }
    
    /**
     * bz771203
     * creates batch-execution command with insert. Marshalls it to XML 
     * and send to drools
     */
   @Test
    public void testInsert() throws Exception {
      
        Person p = new Person("Alice", "spicy meals", 30);
        List<Command> commands = new ArrayList<Command>();
        commands.add(CommandFactory.newInsert(p, "tempPerson"));
        BatchExecutionCommand command = CommandFactory.newBatchExecution(commands);
        String xmlCommand = template.requestBody("direct:marshall", command, String.class);

        String xml = template.requestBody("direct:test-session", xmlCommand, String.class);
        ExecutionResults res = (ExecutionResults) template.requestBody("direct:unmarshall", xml);

        Object o = res.getFactHandle("tempPerson");
        assertTrue("returned String instead of FactHandle instance", o instanceof FactHandle);       
    }
    
    @Test
    public void testInsertElements() {
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("John", "nobody", 50));
        persons.add(new Person("Peter", "himself", 24));
        
        insertElements(persons);
        assertEquals(2, ksession.getFactCount());
    }
    
    private void insertElements(List<Person> objects) {
        String insertElements = 
            "<batch-execution>\n"
            + "  <insert-elements return-objects=\"true\">\n";
        for(Person p : objects) {
            insertElements += "    <objects xsi:type=\"person\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "      <age>" + p.getAge() + "</age>\n"
                + "      <likes>" + p.getLikes() + "</likes>\n"
                + "      <name>" + p.getName() + "</name>\n"
                + "    </objects>\n";    
        }
        insertElements += 
            "  </insert-elements>\n"
            + "</batch-execution>";
        template.requestBody("direct:test-session", insertElements, String.class);
    }
}
