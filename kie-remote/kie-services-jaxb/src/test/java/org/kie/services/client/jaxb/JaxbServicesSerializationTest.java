package org.kie.services.client.jaxb;

import static org.junit.Assert.*;
import static org.kie.services.client.serialization.JaxbSerializationProvider.split;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Assume;
import org.junit.Test;
import org.kie.services.client.AbstractServicesSerializationTest;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;

public class JaxbServicesSerializationTest extends AbstractServicesSerializationTest {

    private static Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.services.client"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

    public TestType getType() {
        return TestType.JAXB;
    }

    protected JaxbSerializationProvider jaxbProvider = new JaxbSerializationProvider();
    { 
        jaxbProvider.setPrettyPrint(true);
    }

    @Override
    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClasses(extraClass);
    }

    public Object testRoundTrip(Object in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return jaxbProvider.deserialize(xmlObject);
    }

    @Test
    public void acceptedCommandsTest() throws Exception {
        for (Class<?> cmdClass : AcceptedCommands.getSet()) {
            try {
                cmdClass.getConstructor(new Class[0]);
            } catch (Exception e) {
                fail("Class " + cmdClass.getSimpleName() + " does not have a no-arg constructor.");
            }
        }
    }

    @Test
    public void jaxbClassesAreKnownToJaxbSerializationProvider() throws Exception { 
        for (Class<?> jaxbClass : reflections.getTypesAnnotatedWith(XmlRootElement.class)) {
            Constructor<?> construct = jaxbClass.getConstructor(new Class [] {});
            Object jaxbInst = construct.newInstance(new Object [] {});
            testRoundTrip(jaxbInst);
        } 
    }
    
    /**
     * If you think this test is a mistake: beware, this test is smarter than you. Seriously. 
     * Heck, this test is smarter than *me*, and I wrote it!
     * 
     * @throws Exception
     */
    @Test
    public void acceptedCommandsCanBeSerializedTest() throws Exception {
        // Only neccessary to run once
        Assume.assumeTrue(getType().equals(TestType.JAXB));

        Field commandsField = JaxbCommandsRequest.class.getDeclaredField("commands");
        XmlElements xmlElemsAnno = (XmlElements) commandsField.getAnnotations()[0];
        XmlElement[] xmlElems = xmlElemsAnno.value();

        Set<Class> cmdSet = new HashSet<Class>(AcceptedCommands.getSet());
        assertEquals(AcceptedCommands.class.getSimpleName() + " contains a different set of Commands than " + JaxbCommandsRequest.class.getSimpleName(),
                cmdSet.size(), xmlElems.length);
        Set<String> xmlElemNameSet = new HashSet<String>();
        for (XmlElement xmlElemAnno : xmlElems) {
            Class cmdClass = xmlElemAnno.type();
            String name = xmlElemAnno.name();
            assertTrue(name + " is used twice as a name.", xmlElemNameSet.add(name));
            assertTrue(cmdClass.getSimpleName() + " is present in " + AcceptedCommands.class.getSimpleName() + " but not in "
                    + JaxbCommandsRequest.class.getSimpleName(), cmdSet.remove(cmdClass));
        }
        for (Class cmdClass : cmdSet) {
            logger.error("Missing: " + cmdClass.getSimpleName());
        }
        assertEquals("See output for classes in " + AcceptedCommands.class.getSimpleName() + " that are not in "
                + JaxbCommandsRequest.class.getSimpleName(), 0, cmdSet.size());
    }

    /**
     * This test is the above one's little brother, and he takes after him. Damn.. these are some smart tests, yo! 
     * 
     * (HA!)
     */
    @Test
    public void allCommandResponseTypesNeedXmlElemIdTest() throws Exception {
        Field commandsField = JaxbCommandsResponse.class.getDeclaredField("responses");
        XmlElements xmlElemsAnno = (XmlElements) commandsField.getAnnotations()[0];
        XmlElement[] xmlElems = xmlElemsAnno.value();

        Set<Class<?>> cmdSet = new HashSet<Class<?>>();
        for (Class<?> cmdRespImpl : reflections.getSubTypesOf(JaxbCommandResponse.class)) {
            cmdSet.add(cmdRespImpl);
        }
        cmdSet.remove(AbstractJaxbCommandResponse.class);

        int numAnnos = xmlElems.length;
        int numClass = cmdSet.size();

        Set<String> xmlElemNameSet = new HashSet<String>();
        for (XmlElement xmlElemAnno : xmlElems) {
            Class cmdClass = xmlElemAnno.type();
            String name = xmlElemAnno.name();
            assertTrue(name + " is used twice as a name.", xmlElemNameSet.add(name));
            assertTrue(cmdClass.getSimpleName() + " is present in " + JaxbCommandsResponse.class.getSimpleName() + " but does not "
                    + "implement " + JaxbCommandResponse.class.getSimpleName(), cmdSet.remove(cmdClass));
        }
        for (Class cmdClass : cmdSet) {
            logger.error("Missing: " + cmdClass.getSimpleName());
        }
        assertTrue("See above output for difference between " + JaxbCommandResponse.class.getSimpleName() + " implementations "
                + "and classes listed in " + JaxbCommandsResponse.class.getSimpleName(), cmdSet.size() == 0);

        assertEquals((numClass > numAnnos ? "Not all classes" : "Non " + JaxbCommandResponse.class.getSimpleName() + " classes")
                + " are listed in the " + JaxbCommandResponse.class.getSimpleName() + ".response @XmlElements list.", numClass,
                numAnnos);
    }

    @Test
    public void uniqueRootElementTest() throws Exception {
        Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.jbpm.kie.services.client"),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner());
        Set<String> idSet = new HashSet<String>();
        HashMap<String, Class> idClassMap = new HashMap<String, Class>();
        for (Class<?> jaxbClass : reflections.getTypesAnnotatedWith(XmlRootElement.class)) {
            XmlRootElement rootElemAnno = jaxbClass.getAnnotation(XmlRootElement.class);
            String id = rootElemAnno.name();
            if ("##default".equals(id)) {
                continue;
            }
            String otherClass = (idClassMap.get(id) == null ? "null" : idClassMap.get(id).getName());
            assertTrue("ID '" + id + "' used in both " + jaxbClass.getName() + " and " + otherClass, idSet.add(id));
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
        Set<Class<?>> copyClasses = JaxbSerializationProvider.commaSeperatedStringToClassSet(commaString);
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
        extraJaxbClasses.add(JaxbServicesSerializationTest.class);
        testRoundTripClassesSet(extraJaxbClasses);
        
        // 2 
        extraJaxbClasses.add(JsonServicesSerializationTest.class);
        testRoundTripClassesSet(extraJaxbClasses);
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
        
        Set<Class<?>> copyExtraJaxbClasses = JaxbSerializationProvider.commaSeperatedStringToClassSet(classesStrProp);
        assertNotNull( "Round-tripped classes set is null!", copyExtraJaxbClasses );
        assertTrue( "Round-tripped classes set is empty!", ! copyExtraJaxbClasses.isEmpty() || emptySet );
        
        assertEquals( "Round-tripped classes size is incorrect!", extraJaxbClasses.size(), copyExtraJaxbClasses.size() );
        
        for( Class<?>  origClass : extraJaxbClasses ) { 
           assertTrue( "Round-tripped class set did not contain " + origClass.getSimpleName(), copyExtraJaxbClasses.remove(origClass) );
        }
        assertTrue( "There is " + copyExtraJaxbClasses.size() + " class left over in the round-tripped class set!", copyExtraJaxbClasses.isEmpty() );
    }
}
