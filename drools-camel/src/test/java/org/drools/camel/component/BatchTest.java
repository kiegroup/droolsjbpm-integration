package org.drools.camel.component;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.custommonkey.xmlunit.XMLUnit;
import org.drools.camel.testdomain.ChangeCollector;
import org.drools.camel.testdomain.Cheese;
import org.drools.camel.testdomain.Person;
import org.drools.command.runtime.rule.ModifyCommand;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalRuleBase;
import org.drools.core.util.StringUtils;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.ResourceType;
import org.kie.definition.KnowledgePackage;
import org.kie.io.Resource;
import org.kie.io.ResourceFactory;
import org.kie.runtime.CommandExecutor;
import org.kie.runtime.ExecutionResults;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.StatelessKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemHandler;
import org.kie.runtime.process.WorkItemManager;
import org.kie.runtime.process.WorkflowProcessInstance;
import org.kie.runtime.rule.FactHandle;
import org.mvel2.templates.SimpleTemplateRegistry;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.templates.res.Node;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@RunWith(JUnit4.class)
public abstract class BatchTest extends ContextTestSupport {
    protected GridNode        node;
    protected CommandExecutor exec;
    protected String          dataformat;
    protected String          copyToDataFormat;

    public BatchTest() {
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                JaxbDataFormat jaxbDf = new JaxbDataFormat();
                jaxbDf.setContextPath( "org.kie.camel.testdomain" );

                from( "direct:exec" ).policy( new DroolsPolicy() ).unmarshal( dataformat ).to( "drools://node/ksession1" ).marshal( dataformat );
                from( "direct:execWithLookup" ).policy( new DroolsPolicy() ).unmarshal( dataformat ).to( "drools://node" ).marshal( dataformat );
                from( "direct:unmarshal" ).policy( new DroolsPolicy() ).unmarshal( dataformat );
                from( "direct:marshal" ).policy( new DroolsPolicy() ).marshal( dataformat );
                from( "direct:to-xstream" ).policy( new DroolsPolicy() ).unmarshal( dataformat ).marshal( "xstream" );
                from( "direct:to-jaxb" ).policy( new DroolsPolicy() ).unmarshal( dataformat ).marshal( jaxbDf );
            }
        };
    }

    public String prettyPrintXml(String xmlSource) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( xmlSource ) ) );

            TransformerFactory tfactory = TransformerFactory.newInstance();
            tfactory.setAttribute( "indent-number",
                                   4 );
            Transformer serializer;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            serializer = tfactory.newTransformer();
            serializer.setOutputProperty( OutputKeys.INDENT,
                                          "yes" );
            serializer.transform( new DOMSource( doc ),
                                  new StreamResult( new OutputStreamWriter( baos,
                                                                            "UTF-8" ) ) );
            return new String( baos.toByteArray() );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    protected Context createJndiContext() throws Exception {
        Context context = super.createJndiContext();

        GridImpl grid = new GridImpl( new HashMap() );
        node = grid.createGridNode( "node" );
        node.set( "ksession1",
                  this.exec );
        context.bind( "node",
                      node );
        return context;
    }

    public void setExec(CommandExecutor exec) {
        this.exec = exec;
        try {
            super.setUp();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    protected final TemplateRegistry tempReg = new SimpleTemplateRegistry();

    protected PrintWriter            writer;

    @Before
    public void before() throws Exception {
        tempReg.addNamedTemplate( "tempReg",
                                  TemplateCompiler.compileTemplate( getClass().getResourceAsStream( dataformat + ".mvt" ),
                                                                    (Map<String, Class<? extends Node>>) null ) );
        TemplateRuntime.execute( tempReg.getNamedTemplate( "tempReg" ),
                                 null,
                                 tempReg );

        XMLUnit.setIgnoreComments( true );
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder( true );
        XMLUnit.setNormalizeWhitespace( true );
        XMLUnit.setNormalize( true );

        if ( !StringUtils.isEmpty( copyToDataFormat ) ) {
            writer = new PrintWriter( new BufferedWriter( new FileWriter( copyToDataFormat + ".mvt",
                                                                          true ) ) );
        }
    }

    @After
    public void after() throws Exception {
        if ( !StringUtils.isEmpty( copyToDataFormat ) ) {
            writer.close();
        }
    }

    public String getContent(String name,
                             String... vars) {
        Map<String, String> map = new HashMap<String, String>();
        int counter = 1;
        for ( String var : vars ) {
            map.put( "var" + counter++,
                     var );
        }

        if ( !StringUtils.isEmpty( copyToDataFormat ) ) {
            writer.println();
            writer.println( "@declare{\"" + name + "\"}" );
        }
        String s = (String) TemplateRuntime.execute( tempReg.getNamedTemplate( name ),
                                                     map );
        if ( !StringUtils.isEmpty( copyToDataFormat ) ) {
            writer.print( prettyPrintXml( template.requestBody( "direct:to-" + copyToDataFormat,
                                                                s,
                                                                String.class ) ) );
            writer.println( "@end{}" );
        }

        return roundTripFromXml( s.trim() );
    }

    public String execContent(String name) {
        return execContent( name,
                            String.class );
    }

    public <T> T execContent(String name,
                             Class<T> cls) {
        return execContent( name,
                            cls,
                            new String[0] );
    }

    public <T> T execContent(String name,
                             Class<T> cls,
                             String... vars) {
        String s = execContent( name,
                                vars );
        if ( cls.isAssignableFrom( String.class ) ) {
            return (T) s;
        } else {
            return unmarshalOutXml( s,
                                    cls );
        }
    }

    public String execContent(String name,
                              String... vars) {
        String inXml = getContent( name,
                                   vars );
        return execInXml( inXml );
    }

    public String execInXml(String inXml) {
        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        return roundTripFromXml( outXml );
    }

    public <T> T unmarshalOutXml(String outXml,
                                 Class<T> cls) {
        Object object = template.requestBody( "direct:unmarshal",
                                              outXml,
                                              Object.class );
        return (T) roundTripFromObject( object );
    }

    public abstract void assertXMLEqual(String expectedXml,
                                        String resultXml);

    public String roundTripFromXml(String inXml) {
        Object object = template.requestBody( "direct:unmarshal",
                                              inXml );
        inXml = template.requestBody( "direct:marshal",
                                      object,
                                      String.class );
        object = template.requestBody( "direct:unmarshal",
                                       inXml );
        return template.requestBody( "direct:marshal",
                                     object,
                                     String.class );
    }

    public Object roundTripFromObject(Object object) {
        String inXml = template.requestBody( "direct:marshal",
                                             object,
                                             String.class );
        object = template.requestBody( "direct:unmarshal",
                                       inXml );
        inXml = template.requestBody( "direct:marshal",
                                      object,
                                      String.class );
        return template.requestBody( "direct:unmarshal",
                                     inXml );
    }

    private StatelessKnowledgeSession getStatelessKnowledgeSession(Resource resource) throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( resource,
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }

        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages( pkgs );
        StatelessKnowledgeSession session = kbase.newStatelessKnowledgeSession();

        return session;
    }

    private StatefulKnowledgeSession getStatefulKnowledgeSession(Resource resource) throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( resource,
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors() );
            throw new RuntimeException( kbuilder.getErrors().toString() );
        }

        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages( pkgs );
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        return session;
    }

    @Test
    public void testListenForChanges() throws Exception {
        String str = "";
        str += "package org.kie.camel.testdomain \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "import org.kie.camel.testdomain.ChangeCollector \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese(price==25) \n";
        str += " \n";
        str += "  then \n";
        str += "end\n";

        str += "rule rule2 \n";
        str += "  when \n";
        str += "    p : Person(name=='mic') \n";
        str += "    c : Cheese(price != 42) \n";
        str += " \n";
        str += "  then \n";
        str += "    c.setPrice( 42 ); \n";
        str += "    update(c); \n";
        str += "end\n";

        str += "rule rule3 \n";
        str += "  when \n";
        str += "    p : Person(name=='mark') \n";
        str += "    c : Cheese(price == 42) \n";
        str += " \n";
        str += "  then \n";
        str += "    retract(c); \n";
        str += "end\n";

        str += "rule ruleBootStrap \n";
        str += "salience 10000\n";
        str += "  when \n";
        str += "    $c : ChangeCollector() \n";
        str += " \n";
        str += "  then \n";
        str += "    kcontext.getKnowledgeRuntime().addEventListener($c); \n";
        str += "end\n";

        str += "rule ruleCleanup \n";
        str += "salience -10000\n";
        str += "  when \n";
        str += "    $c : ChangeCollector() \n";
        str += " \n";
        str += "  then \n";
        str += "    kcontext.getKnowledgeRuntime().removeEventListener($c); \n";
        str += "    retract($c); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testListenForChanges.in.1" );

        outXml = execContent( "testListenForChanges.in.2" );
        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        assertXMLEqual( getContent( "testListenForChanges.expected.1",
                                    ((FactHandle) result.getFactHandle( "changes" )).toExternalForm(),
                                    ((FactHandle) result.getFactHandle( "person" )).toExternalForm() ),
                        outXml );

        ChangeCollector collector = (ChangeCollector) result.getValue( "changes" );
        Cheese c = (Cheese) collector.getChanges().get( 0 );
        assertEquals( 42,
                      c.getPrice() );

        result = execContent( "testListenForChanges.in.3",
                              ExecutionResults.class );

        collector = (ChangeCollector) result.getValue( "changes" );
        assertEquals( "stilton",
                      collector.getRetracted().get( 0 ) );

    }

    @Test
    public void testInsertWithDefaults() throws Exception {

        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testInsertWithDefaults.in.1" );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

        String expectedXml = getContent( "testInsertWithDefaults.expected.1",
                                         ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() );

        assertXMLEqual( expectedXml,
                        outXml );
    }

    @Test
    public void testInsertWithReturnObjectFalse() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testInsertWithReturnObjectFalse.in.1" );
        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        String expectedXml = getContent( "testInsertWithReturnObjectFalse.expected.1",
                                         ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() );

        assertXMLEqual( expectedXml,
                        outXml );
    }

    @Test
    public void testFactHandleReturn() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    System.err.println(42); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        FactHandle fh = ksession.insert( new Person( "mic",
                                                     42 ) );
        List<FactHandle> list = new ArrayList<FactHandle>();
        list.add( fh );

        ksession.setGlobal( "list1",
                            list );

        String outXml = execContent( "testFactHandleReturn.in.1" );

        assertXMLEqual( getContent( "testFactHandleReturn.expected.1",
                                    fh.toExternalForm() ),
                        outXml );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        List outList = (List) result.getValue( "out-list" );
        assertEquals( 1,
                      outList.size() );
        assertEquals( fh.toExternalForm(),
                      ((FactHandle) outList.get( 0 )).toExternalForm() );
        assertNotSame( fh,
                       outList.get( 0 ) );
    }

    @Test
    public void testGetObject() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        ExecutionResults result = execContent( "testGetObject.in.1",
                                               ExecutionResults.class );

        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        String outXml = execContent( "testGetObject.in.2",
                                     ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() );
        result = unmarshalOutXml( outXml,
                                  ExecutionResults.class );

        stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );
    }

    @Test
    public void testRetractObject() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        ExecutionResults result = execContent( "testRetractObject.in.1",
                                               ExecutionResults.class );

        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        String outXml = execContent( "testRetractObject.in.2",
                                     ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() );

        outXml = execContent( "testRetractObject.in.3",
                              ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() );
        result = unmarshalOutXml( outXml,
                                  ExecutionResults.class );

        assertNull( result.getValue( "outStilton" ) );
    }

    @Test
    public void testModifyObject() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testModifyObject.in.1" );
        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        String stiltonfh = ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm();

        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        String expectedXml = getContent( "testModifyObject.expected.1",
                                         stiltonfh );

        assertXMLEqual( expectedXml,
                        outXml );

        execContent( "testModifyObject.in.2",
                     ExecutionResults.class,
                     stiltonfh );

        result = execContent( "testModifyObject.in.3",
                              ExecutionResults.class,
                              stiltonfh );

        stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 42,
                      stilton.getOldPrice() );
        assertEquals( 55,
                      stilton.getPrice() );

        //now test for code injection:
        ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = false;

        execContent( "testModifyObject.in.4",
                     ExecutionResults.class,
                     stiltonfh );

        ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = true;

        // should be the same as before
        result = execContent( "testModifyObject.in.5",
                              ExecutionResults.class,
                              stiltonfh );

        // The value gets turned into a literal to avoid injection
        stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( "throwException()",
                      stilton.getType() );

    }

    @Test
    public void testInsertElements() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "     list1.add( $c );";
        str += "end\n";
        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testInsertElements.in.1" );

        assertXMLEqual( getContent( "testInsertElements.expected.1" ),
                        outXml );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        List list = (List) result.getValue( "list1" );
        Cheese stilton25 = new Cheese( "stilton",
                                       30 );
        Cheese stilton30 = new Cheese( "stilton",
                                       35 );

        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );

        assertEquals( expectedList,
                      new HashSet( list ) );
    }

    @Test
    public void testInsertElementsWithReturnObjects() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "    list1.add( $c );";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testInsertElementsWithReturnObjects.in.1" );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        List list1 = (List) result.getValue( "list1" );
        assertEquals( 2,
                      list1.size() );
        assertTrue( list1.contains( new Cheese( "stilton",
                                                35 ) ) );
        assertTrue( list1.contains( new Cheese( "stilton",
                                                30 ) ) );

        List myFacts = (List) result.getValue( "myfacts" );
        assertEquals( 2,
                      list1.size() );
        assertTrue( myFacts.contains( new Cheese( "stilton",
                                                  35 ) ) );
        assertTrue( myFacts.contains( new Cheese( "stilton",
                                                  30 ) ) );

        List<FactHandle> factHandles = (List<FactHandle>) result.getFactHandle( "myfacts" );
        List list = new ArrayList();
        list.add( ksession.getObject( ((InternalFactHandle) factHandles.get( 0 )) ) );
        list.add( ksession.getObject( ((InternalFactHandle) factHandles.get( 1 )) ) );
        assertTrue( list.contains( new Cheese( "stilton",
                                               35 ) ) );
        assertTrue( list.contains( new Cheese( "stilton",
                                               30 ) ) );

        assertXMLEqual( getContent( "testInsertElementsWithReturnObjects.expected.1",
                                    factHandles.get( 0 ).toExternalForm(),
                                    factHandles.get( 1 ).toExternalForm() ),
                        outXml );
    }

    @Test
    public void testSetGlobal() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "global java.util.List list2 \n";
        str += "global java.util.List list3 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( 30 ); \n";
        str += "    list1.add( $c ); \n";
        str += "    list2.add( $c ); \n";
        str += "    list3.add( $c ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testSetGlobal.in.1" );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        assertXMLEqual( getContent( "testSetGlobal.expected.1",
                                    ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() ),
                        outXml );

        Cheese stilton = new Cheese( "stilton",
                                     30 );

        assertNull( result.getValue( "list1" ) );

        List list2 = (List) result.getValue( "list2" );
        assertEquals( 1,
                      list2.size() );
        assertEquals( stilton,
                      list2.get( 0 ) );

        List list3 = (List) result.getValue( "outList3" );
        assertEquals( 1,
                      list3.size() );
        assertEquals( stilton,
                      list3.get( 0 ) );
    }

    @Test
    public void testGetGlobal() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    list1.add( $c ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testGetGlobal.in.1" );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        assertXMLEqual( getContent( "testGetGlobal.expected.1",
                                    ((FactHandle) result.getFactHandle( "outStilton" )).toExternalForm() ),
                        outXml );

        List resultsList = (List) result.getValue( "out-list" );
        assertEquals( 1,
                      resultsList.size() );
        assertEquals( new Cheese( "stilton",
                                  25 ),
                      resultsList.get( 0 ) );

    }

    @Test
    public void testGetObjects() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml = "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"insert-elements\":{\"objects\":[";
        inXml += "   {   \"org.kie.camel.testdomain.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, ";
        inXml += "   {   \"org.kie.camel.testdomain.Cheese\":{\"type\":\"stilton\",\"price\":30,\"oldPrice\":0}} ";
        inXml += "   ]}}";
        inXml += ",  {\"get-objects\":{\"out-identifier\":\"list1\"}}";
        inXml += "]}}";

        StatelessKnowledgeSession ksession = getStatelessKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testGetObjects.in.1" );

        // Can't compare content a hashmap of objects keeps changing order
        // assertXMLEqual( getContent( "testGetObjects.expected.1" ),
        //                outXml );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        List list = (List) result.getValue( "list1" );
        Cheese stilton25 = new Cheese( "stilton",
                                       30 );
        Cheese stilton30 = new Cheese( "stilton",
                                       35 );

        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );

        assertEquals( expectedList,
                      new HashSet( list ) );
    }

    @Test
    public void testQuery() throws Exception {
        String str = "";
        str += "package org.kie.test  \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "query cheeses \n";
        str += "    stilton : Cheese(type == 'stilton') \n";
        str += "    cheddar : Cheese(type == 'cheddar', price == stilton.price) \n";
        str += "end\n";
        str += "query cheesesWithParams(String a, String b) \n";
        str += "    stilton : Cheese(type == a) \n";
        str += "    cheddar : Cheese(type == b, price == stilton.price) \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testQuery.in.1" );

        // Order is not determinstic, so can't test.
        //         assertXMLEqual( getContent( "testQuery.expected.1" ),
        //                        outXml );
        getContent( "testQuery.expected.1" ); // just to force a tostring for comparison

        ExecutionResults batchResult = unmarshalOutXml( outXml,
                                                        ExecutionResults.class );

        Cheese stilton1 = new Cheese( "stilton",
                                      1 );
        Cheese cheddar1 = new Cheese( "cheddar",
                                      1 );
        Cheese stilton2 = new Cheese( "stilton",
                                      2 );
        Cheese cheddar2 = new Cheese( "cheddar",
                                      2 );

        Set set = new HashSet();
        List list = new ArrayList();
        list.add( stilton1 );
        list.add( cheddar1 );
        set.add( list );

        list = new ArrayList();
        list.add( stilton2 );
        list.add( cheddar2 );
        set.add( list );

        org.kie.runtime.rule.QueryResults results = (org.kie.runtime.rule.QueryResults) batchResult.getValue( "cheeses" );
        assertEquals( 2,
                      results.size() );
        assertEquals( 2,
                      results.getIdentifiers().length );
        Set newSet = new HashSet();
        for ( org.kie.runtime.rule.QueryResultsRow result : results ) {
            list = new ArrayList();
            list.add( result.get( "stilton" ) );
            list.add( result.get( "cheddar" ) );
            newSet.add( list );
        }
        assertEquals( set,
                      newSet );
    }

    @Test
    public void testManualFireAllRules() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "    list1.add( $c );";
        str += "end\n";

        StatelessKnowledgeSession ksession = getStatelessKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testManualFireAllRules.in.1" );

        ExecutionResults result = unmarshalOutXml( outXml,
                                                   ExecutionResults.class );

        assertXMLEqual( getContent( "testManualFireAllRules.expected.1",
                                    ((FactHandle) result.getFactHandle( "outBrie" )).toExternalForm() ),
                        outXml );

        // brie should not have been added to the list
        List list = (List) result.getValue( "list1" );
        Cheese stilton25 = new Cheese( "stilton",
                                       30 );
        Cheese stilton30 = new Cheese( "stilton",
                                       35 );

        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );

        assertEquals( expectedList,
                      new HashSet( list ) );

        // brie should not have changed
        Cheese brie10 = new Cheese( "brie",
                                    10 );
        brie10.setOldPrice( 5 );
        assertEquals( brie10,
                      result.getValue( "outBrie" ) );
    }

    @Test
    public void testProcess() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.kie.actions\" package-name=\"org.kie\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <imports>\n";
        str += "      <import name=\"org.kie.camel.testdomain.TestVariable\" />\n";
        str += "    </imports>\n";
        str += "    <globals>\n";
        str += "      <global identifier=\"list1\" type=\"java.util.List\" />\n";
        str += "    </globals>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"person\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"TestVariable\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <actionNode id=\"2\" name=\"MyActionNode\" >\n";
        str += "      <action type=\"expression\" dialect=\"mvel\" >System.out.println(\"Triggered\");\n";
        str += "list1.add(person.name);\n";
        str += "</action>\n";
        str += "    </actionNode>\n";
        str += "    <end id=\"3\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"2\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "  </connections>\n" + "\n";
        str += "</process>";

        Reader source = new StringReader( str );
        kbuilder.add( ResourceFactory.newReaderResource( source ),
                      ResourceType.DRF );
        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List<String> list = new ArrayList<String>();
        ksession.setGlobal( "list1",
                            list );
        setExec( ksession );

        String outXml = execContent( "testProcess.in.1" );

        assertXMLEqual( getContent( "testProcess.expected.1" ),
                        outXml );

        ExecutionResults results = unmarshalOutXml( outXml,
                                                    ExecutionResults.class );

        list = (List) results.getValue( "out-list" );

        assertEquals( 1,
                      list.size() );
        assertEquals( "John Doe",
                      list.get( 0 ) );
    }

    @Test
    public void testProcessInstanceSignalEvent() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.kie.event\" package-name=\"org.kie\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"MyVar\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>SomeText</value>\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <eventNode id=\"2\" name=\"Event\" variableName=\"MyVar\" >\n";
        str += "      <eventFilters>\n";
        str += "        <eventFilter type=\"eventType\" eventType=\"MyEvent\" />\n";
        str += "      </eventFilters>\n";
        str += "    </eventNode>\n";
        str += "    <join id=\"3\" name=\"Join\" type=\"1\" />\n";
        str += "    <end id=\"4\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"3\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "    <connection from=\"3\" to=\"4\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRF );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        setExec( ksession );

        ProcessInstance processInstance = ksession.startProcess( "org.kie.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );

        execContent( "testProcessInstanceSignalEvent.in.1" );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    @Test
    public void testProcessRuntimeSignalEvent() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.kie.event\" package-name=\"org.kie\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"MyVar\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>SomeText</value>\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <eventNode id=\"2\" name=\"Event\" scope=\"external\" variableName=\"MyVar\" >\n";
        str += "      <eventFilters>\n";
        str += "        <eventFilter type=\"eventType\" eventType=\"MyEvent\" />\n";
        str += "      </eventFilters>\n";
        str += "    </eventNode>\n";
        str += "    <join id=\"3\" name=\"Join\" type=\"1\" />\n";
        str += "    <end id=\"4\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"3\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "    <connection from=\"3\" to=\"4\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRF );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        setExec( ksession );

        ProcessInstance processInstance = ksession.startProcess( "org.kie.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );

        execContent( "testProcessRuntimeSignalEvent.in.1" );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    @Test
    public void testCompleteWorkItem() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.kie.actions\" package-name=\"org.kie\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"UserName\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>John Doe</value>\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Person\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"org.kie.camel.testdomain.Person\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"MyObject\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Number\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.IntegerDataType\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <workItem id=\"2\" name=\"HumanTask\" >\n";
        str += "      <work name=\"Human Task\" >\n";
        str += "        <parameter name=\"ActorId\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{UserName}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Content\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{Person.name}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"TaskName\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>Do something</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Priority\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Comment\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Attachment\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "        </parameter>\n";
        str += "      </work>\n";
        str += "      <mapping type=\"in\" from=\"MyObject\" to=\"Attachment\" />";
        str += "      <mapping type=\"in\" from=\"Person.name\" to=\"Comment\" />";
        str += "      <mapping type=\"out\" from=\"Result\" to=\"MyObject\" />";
        str += "      <mapping type=\"out\" from=\"Result.length()\" to=\"Number\" />";
        str += "    </workItem>\n";
        str += "    <end id=\"3\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"2\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        Reader source = new StringReader( str );
        kbuilder.add( ResourceFactory.newReaderResource( source ),
                      ResourceType.DRF );

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRF );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        setExec( ksession );

        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler( "Human Task",
                                                               handler );
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put( "UserName",
                        "John Doe" );
        Person person = new Person();
        person.setName( "John Doe" );
        parameters.put( "Person",
                        person );
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess( "org.kie.actions",
                                                                                                   parameters );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        assertEquals( "John Doe",
                      workItem.getParameter( "ActorId" ) );
        assertEquals( "John Doe",
                      workItem.getParameter( "Content" ) );
        assertEquals( "John Doe",
                      workItem.getParameter( "Comment" ) );

        assertEquals( WorkItem.PENDING,
                      workItem.getState() );

        execContent( "testCompleteWorkItem.in.1",
                     Long.toString( workItem.getId() ) );

        assertEquals( WorkItem.COMPLETED,
                      workItem.getState() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );

        parameters = new HashMap<String, Object>();
        parameters.put( "UserName",
                        "Jane Doe" );
        parameters.put( "MyObject",
                        "SomeString" );
        person = new Person();
        person.setName( "Jane Doe" );
        parameters.put( "Person",
                        person );
        processInstance = (WorkflowProcessInstance) ksession.startProcess( "org.kie.actions",
                                                                           parameters );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        assertEquals( "Jane Doe",
                      workItem.getParameter( "ActorId" ) );
        assertEquals( "SomeString",
                      workItem.getParameter( "Attachment" ) );
        assertEquals( "Jane Doe",
                      workItem.getParameter( "Content" ) );
        assertEquals( "Jane Doe",
                      workItem.getParameter( "Comment" ) );

        assertEquals( WorkItem.PENDING,
                      workItem.getState() );

        execContent( "testCompleteWorkItem.in.2",
                     Long.toString( workItem.getId() ) );

        assertEquals( WorkItem.COMPLETED,
                      workItem.getState() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "SomeOtherString",
                      processInstance.getVariable( "MyObject" ) );
        assertEquals( 15,
                      processInstance.getVariable( "Number" ) );
    }

    @Test
    public void testAbortWorkItem() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.kie.actions\" package-name=\"org.kie\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"UserName\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>John Doe</value>\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Person\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"org.kie.camel.testdomain.Person\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"MyObject\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Number\" >\n";
        str += "        <type name=\"org.kie.process.core.datatype.impl.type.IntegerDataType\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <workItem id=\"2\" name=\"HumanTask\" >\n";
        str += "      <work name=\"Human Task\" >\n";
        str += "        <parameter name=\"ActorId\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{UserName}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Content\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{Person.name}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"TaskName\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>Do something</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Priority\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Comment\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Attachment\" >\n";
        str += "          <type name=\"org.kie.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "        </parameter>\n";
        str += "      </work>\n";
        str += "      <mapping type=\"in\" from=\"MyObject\" to=\"Attachment\" />";
        str += "      <mapping type=\"in\" from=\"Person.name\" to=\"Comment\" />";
        str += "      <mapping type=\"out\" from=\"Result\" to=\"MyObject\" />";
        str += "      <mapping type=\"out\" from=\"Result.length()\" to=\"Number\" />";
        str += "    </workItem>\n";
        str += "    <end id=\"3\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"2\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        Reader source = new StringReader( str );
        kbuilder.add( ResourceFactory.newReaderResource( source ),
                      ResourceType.DRF );

        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kpkgs );
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        setExec( ksession );

        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler( "Human Task",
                                                               handler );
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put( "UserName",
                        "John Doe" );
        Person person = new Person();
        person.setName( "John Doe" );
        parameters.put( "Person",
                        person );
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess( "org.kie.actions",
                                                                                                   parameters );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );

        assertEquals( WorkItem.PENDING,
                      workItem.getState() );

        execContent( "testAbortWorkItem.in.1",
                     Long.toString( workItem.getId() ) );

        assertEquals( WorkItem.ABORTED,
                      workItem.getState() );
    }

    @Test @Ignore
    public void testInsertObjectWithDeclaredFact() throws Exception {
        String str = "";
        str += "package org.foo \n";
        str += "declare Whee \n\ttype: String\n\tprice: Integer\n\toldPrice: Integer\nend\n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Whee() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        ExecutionResults results = null;
        String outXml = null;
        ClassLoader orig = null;
        ClassLoader cl = ((InternalRuleBase) ((StatefulKnowledgeSessionImpl) ksession).getRuleBase()).getRootClassLoader();
        try {
            orig = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( cl );
            outXml = execContent( "testInsertObjectWithDeclaredFact.in.1" );
            results = unmarshalOutXml( outXml,
                                       ExecutionResults.class );
        } finally {
            Thread.currentThread().setContextClassLoader( orig );
        }
        FactHandle factHandle = (FactHandle) results.getFactHandle( "outStilton" );
        Object object = results.getValue( "outStilton" );

        assertEquals( "org.foo.Whee",
                      object.getClass().getName() );
        assertXMLEqual( getContent( "testInsertObjectWithDeclaredFact.expected.1",
                                    factHandle.toExternalForm() ),
                        outXml );

    }

    @Test @Ignore
    public void testInsertObjectWithDeclaredFactAndQuery() throws Exception {
        String str = "";
        str += "package org.foo \n";
        str += "declare Whee \n\ttype: String\n\tprice: Integer\n\toldPrice: Integer\nend\n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Whee(price < 30) \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n update($c);\n";
        str += "end\n";
        str += "query results\n";
        str += "    w: Whee(price > 0)";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );
        ClassLoader cl = ((InternalRuleBase) ((StatefulKnowledgeSessionImpl) ksession).getRuleBase()).getRootClassLoader();
        ClassLoader orig = null;
        try {
            orig = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( cl );

            String outXml = execContent( "testInsertObjectWithDeclaredFactAndQuery.in.1" );

            ExecutionResults results = unmarshalOutXml( outXml,
                                                        ExecutionResults.class );
            FactHandle fh = (FactHandle) results.getFactHandle( "outStilton" );

            outXml = execContent( "testInsertObjectWithDeclaredFactAndQuery.in.2" );

            assertXMLEqual( getContent( "testInsertObjectWithDeclaredFactAndQuery.expected.1",
                                        fh.toExternalForm() ),
                            outXml );
        } finally {
            Thread.currentThread().setContextClassLoader( orig );
        }
    }

    @Test
    public void testExecutionNodeLookup() throws Exception {
        String str = "";
        str += "package org.kie \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        StatefulKnowledgeSession ksession = getStatefulKnowledgeSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = execContent( "testExecutionNodeLookup.in.1" );
        ExecutionResults results = unmarshalOutXml( outXml,
                                                    ExecutionResults.class );

        Cheese stilton = (Cheese) results.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) results.getFactHandle( "outStilton" );
        stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

        assertXMLEqual( getContent( "testExecutionNodeLookup.expected.1",
                                    factHandle.toExternalForm() ),
                        outXml );
    }

    public static class TestWorkItemHandler
        implements
        WorkItemHandler {
        private WorkItem workItem;

        public void executeWorkItem(WorkItem workItem,
                                    WorkItemManager manager) {
            this.workItem = workItem;
        }

        public void abortWorkItem(WorkItem workItem,
                                  WorkItemManager manager) {
        }

        public WorkItem getWorkItem() {
            return workItem;
        }
    }

}
