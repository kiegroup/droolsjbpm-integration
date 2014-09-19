package org.kie.services.client.serialization;

import static org.kie.services.client.serialization.JaxbSerializationProvider.split;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.jaxb.ComparePair;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.internal.task.api.model.SubTasksStrategy;
import org.kie.services.client.AbstractRemoteSerializationTest;
import org.kie.services.client.jaxb.JsonRemoteSerializationTest;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class JaxbRemoteSerializationTest extends AbstractRemoteSerializationTest {

    private static final String PROCESS_INSTANCE_ID_NAME = "process-instance-id";

    private static Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.services.client"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

    public TestType getType() {
        return TestType.JAXB;
    }

    protected JaxbSerializationProvider jaxbProvider = JaxbSerializationProvider.clientSideInstance();
    { 
        jaxbProvider.setPrettyPrint(true);
    }

    @Override
    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClasses(true, extraClass);
    }

    @Override
    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }

    @Test
    public void jaxbClassesAreKnownToJaxbSerializationProvider() throws Exception {
        int i = 0;
        for (Class<?> jaxbClass : reflections.getTypesAnnotatedWith(XmlRootElement.class)) {
            ++i;
            Constructor<?> construct = jaxbClass.getConstructor(new Class [] {});
            Object jaxbInst = construct.newInstance(new Object [] {});
            testRoundTrip(jaxbInst);
        }
        assertTrue( i > 20 );
    }

    @Test
    public void uniqueRootElementTest() throws Exception {
        Reflections reflections = new Reflections(
                ClasspathHelper.forPackage("org.kie.services"),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner());
        Set<String> idSet = new HashSet<String>();
        HashMap<String, Class> idClassMap = new HashMap<String, Class>();
        for (Class<?> jaxbClass : reflections.getTypesAnnotatedWith(XmlRootElement.class)) {
            if( ! jaxbClass.getPackage().getName().startsWith("org.kie") ) { 
                continue;
            }
            XmlRootElement rootElemAnno = jaxbClass.getAnnotation(XmlRootElement.class);
            String id = rootElemAnno.name();
            if ("##default".equals(id)) {
                continue;
            }
            String otherClass = (idClassMap.get(id) == null ? "null" : idClassMap.get(id).getName());
            if( ! id.equals("deployment-descriptor") ) {
                assertTrue("ID '" + id + "' used in both " + jaxbClass.getName() + " and " + otherClass, idSet.add(id));
            }
            idClassMap.put(id, jaxbClass);
        }
    }

    @Test
    public void classListPropertyTest() throws Exception {
        String in = "1";
        String[] out;
        String again;

        out = split(in);
        assertEquals(1, out.length);
        assertEquals("1", out[0]);
        again = join(out);
        assertEquals(in, again);

        out = split(" 1 ");
        assertEquals(1, out.length);
        assertEquals("1", out[0]);
        again = join(out);
        assertEquals("1", again);

        out = split("1,2");
        assertEquals(2, out.length);
        assertEquals("1", out[0]);
        assertEquals("2", out[1]);
        again = join(out);
        assertEquals(again, "1,2");

        out = split(" 1,2 ");
        assertEquals(2, out.length);
        assertEquals("1", out[0]);
        assertEquals("2", out[1]);
        out[0] = "1 ";
        out[1] = " 2";
        again = join(out);
        assertEquals("1,2", again);

        out = split(" 1, ");
        assertEquals(out.length, 1);
        assertEquals(out[0], "1");
        out[0] = "1 ";
        again = join(out);
        assertEquals("1", again);

        out = split(" , 2   ");
        assertEquals(out.length, 1);
        assertEquals(out[0], "2");
        out[0] = "2 ";
        again = join(out);
        assertEquals("2", again);

        out = split(" ,    ");
        assertEquals(out.length, 0);
        again = join(out);
        assertEquals("", again);

        Set<Class<?>> classList = new HashSet<Class<?>>();
        classList.add(String.class);
        classList.add(Integer.class);
        classList.add(Byte.class);
        classList.add(Short.class);
        classList.add(Long.class);

        String commaString = JaxbSerializationProvider.classSetToCommaSeperatedString(classList);
        Set<Class<?>> copyClasses = JaxbSerializationProvider.commaSeperatedStringToClassSet(this.getClass().getClassLoader(), commaString);
        assertEquals( classList, copyClasses );
        String newCommaString = JaxbSerializationProvider.classSetToCommaSeperatedString(copyClasses);
        assertEquals( commaString.length(), newCommaString.length() );
    }

    private String join(String[] inArr) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < inArr.length; ++i) {
            String temp = inArr[i].trim();
            if (!temp.isEmpty()) {
                if (out.length() > 0) {
                    out.append(",");
                }
                out.append(temp);
            }
        }
        return out.toString();
    }

    @Test
    public void jmsSerializationPropertyTest() {
        // 0
        Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
        testRoundTripClassesSet(extraJaxbClasses);

        // 1
        extraJaxbClasses.add(JaxbRemoteSerializationTest.class);
        testRoundTripClassesSet(extraJaxbClasses);

        // 2
        extraJaxbClasses.add(JsonRemoteSerializationTest.class);
        testRoundTripClassesSet(extraJaxbClasses);
    }

    @Test
    @Ignore
    public void processInstanceIdFieldInCommands() throws Exception {
        Reflections cmdReflections = new Reflections(
                ClasspathHelper.forPackage("org.drools.command.*"),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

        Set<Class<?>> classes = cmdReflections.getTypesAnnotatedWith(XmlRootElement.class);
        Set<Class> cmdClasses = new HashSet<Class>();
        for (Class<?> jaxbClass : classes ) {
            if( jaxbClass.getSimpleName().endsWith("Command") ) {
                cmdClasses.add(jaxbClass);
            }
        }
        for( Class<?> jaxbCmdClass : cmdClasses ) {
            for( Field field : jaxbCmdClass.getDeclaredFields() ) {
                String fullFieldName = jaxbCmdClass.getSimpleName() + "." + field.getName();
                field.setAccessible(true);
                // check that type matches
                XmlElement xmlElem = ((XmlElement) field.getAnnotation(XmlElement.class));
                XmlAttribute xmlAttribute = ((XmlAttribute) field.getAnnotation(XmlAttribute.class));
                if( xmlElem != null ) {
                    String xmlElemName = xmlElem.name();
                    if( xmlElemName != null && xmlElemName.equals(PROCESS_INSTANCE_ID_NAME) ) {
                        assertTrue( fullFieldName + " is an incorrect type! (" + field.getType() + ")",
                                field.getType().equals(Long.class) || field.getType().equals(long.class) );
                    }
                } else if (xmlAttribute != null) {
                    String xmlAttributeName = xmlAttribute.name();
                    if( xmlAttributeName != null && xmlAttributeName.equals(PROCESS_INSTANCE_ID_NAME) ) {
                        assertTrue( fullFieldName + " is an incorrect type! (" + field.getType() + ")",
                                field.getType().equals(Long.class) || field.getType().equals(long.class) );
                    }
                }
                // check that field has correct XmlElement name
                String name = field.getName().toLowerCase();
                if( name.startsWith("proc") && name.contains("inst")
                        && ! name.endsWith("s") && ! name.endsWith("list")) {
                    xmlElem = ((XmlElement) field.getAnnotation(XmlElement.class));
                    xmlAttribute = ((XmlAttribute) field.getAnnotation(XmlAttribute.class));
                    String xmlElemName = null;
                    String xmlAttrName = null;

                    if( xmlElem != null ) {
                        xmlElemName = xmlElem.name();
                    }
                    if( xmlAttribute != null ) {
                        xmlAttrName = xmlAttribute.name();
                    }
                    if( xmlElemName != null ) {
                        assertEquals( fullFieldName + " is incorrectly annotated with name '" + xmlElemName + "'",
                                PROCESS_INSTANCE_ID_NAME, xmlElemName );
                    } else if( xmlAttrName != null ) {
                        assertEquals( fullFieldName + " is incorrectly annotated with name '" + xmlAttrName + "'",
                                PROCESS_INSTANCE_ID_NAME, xmlAttrName );
                    } else {
                        logger.error( "Should " + fullFieldName + " have an @XmlElement or @XmlAttribute annotation?");
                    }

                }
            }

        }
    }
    
    private void testRoundTripClassesSet(Set<Class<?>> extraJaxbClasses ) { 
        boolean emptySet = extraJaxbClasses.isEmpty();
        assertNotNull( "Test class set is null!", extraJaxbClasses);
        String classesStrProp = JaxbSerializationProvider.classSetToCommaSeperatedString(extraJaxbClasses);
        assertNotNull( "Classes list string is null!", classesStrProp );
        assertTrue( "Classes list string is incorrectly formatted!", 
                (! classesStrProp.isEmpty() || emptySet )
                && ( ! classesStrProp.contains(" ") )
                && ( classesStrProp.length() > 10 || emptySet) );
        
        Set<Class<?>> copyExtraJaxbClasses 
            = JaxbSerializationProvider.commaSeperatedStringToClassSet(this.getClass().getClassLoader(), classesStrProp);
        assertNotNull( "Round-tripped classes set is null!", copyExtraJaxbClasses );
        assertTrue( "Round-tripped classes set is empty!", ! copyExtraJaxbClasses.isEmpty() || emptySet );
        
        assertEquals( "Round-tripped classes size is incorrect!", extraJaxbClasses.size(), copyExtraJaxbClasses.size() );
        
        for( Class<?>  origClass : extraJaxbClasses ) { 
           assertTrue( "Round-tripped class set did not contain " + origClass.getSimpleName(), copyExtraJaxbClasses.remove(origClass) );
        }
        assertTrue( "There is " + copyExtraJaxbClasses.size() + " class left over in the round-tripped class set!", copyExtraJaxbClasses.isEmpty() );
    }
    
    @Test
    public void jaxbTaskSummarySerialization() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        TaskSummaryImpl taskSumImpl = new TaskSummaryImpl(
                1l, 
                "a", "b", "c", 
                Status.Completed, 
                3, true, 
                new UserImpl("d"), new UserImpl("e"), 
                new Date(), new Date(), new Date(), 
                "f", 5, 2l, "deploymentId",
                SubTasksStrategy.EndParentOnAllSubTasksEnd, 6l);
        taskSumImpl.setParentId(4l);

        JaxbTaskSummary jaxbTaskSum = new JaxbTaskSummary(taskSumImpl);
        JaxbTaskSummary jaxbTaskSumCopy = testRoundTrip(jaxbTaskSum);

        ComparePair.compareObjectsViaFields(jaxbTaskSum, jaxbTaskSumCopy, "subTaskStrategy", "potentialOwners");
    }
    
}
