package org.kie.services.client.serialization;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

public class JaxbSerializationProvider implements SerializationProvider {

    public final static int JMS_SERIALIZATION_TYPE = 0;

    private static Class<?>[] kieJaxbClasses = { 
        // Command Request/Response
            JaxbCommandsRequest.class, 
            JaxbCommandsResponse.class,
        // REST other
            JaxbGenericResponse.class,
            JaxbProcessInstanceWithVariablesResponse.class,
            JaxbDeploymentJobResult.class,
            JaxbDeploymentUnitList.class
    };

    public static Set<Class<?>> PRIMITIVE_ARRAY_CLASS_SET = new CopyOnWriteArraySet<Class<?>>();
    static { 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Boolean[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Byte[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Character[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Double[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Float[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Integer[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Long[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Math[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Number[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new Short[]{}.getClass()); 
        PRIMITIVE_ARRAY_CLASS_SET.add(new String[]{}.getClass());
    };
    
    private Set<Class<?>> jaxbClasses = new HashSet<Class<?>>(Arrays.asList(kieJaxbClasses));
    { jaxbClasses.addAll(PRIMITIVE_ARRAY_CLASS_SET); }
    
    private Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
    
    private JAXBContext jaxbContext;
    private boolean prettyPrint = false;

    public JaxbSerializationProvider() {
        initializeJaxbContext();
    }

    public JaxbSerializationProvider(Collection<Class<?>> extraJaxbClassList) {
        extraJaxbClassList.addAll(extraJaxbClassList);
        jaxbClasses.addAll(extraJaxbClassList);
        initializeJaxbContext();
    }

    private void initializeJaxbContext() {
        try {
            jaxbContext = JAXBContext.newInstance(jaxbClasses.toArray(new Class[jaxbClasses.size()]));
        } catch (JAXBException jaxbe) {
            throw new UnsupportedOperationException("Unsupported JAXB Class during initialization: " + jaxbe.getMessage(), jaxbe);
        }
    }
    
    public JAXBContext getJaxbContext() { 
        return this.jaxbContext;
    }

    public String serialize(Object object) {
        Marshaller marshaller = null;
        try { 
            marshaller = jaxbContext.createMarshaller();
            if( prettyPrint ) { 
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }
        } catch( JAXBException jaxbe ) { 
            throw new SerializationException("Unable to create JAXB marshaller.", jaxbe);
        }
        StringWriter stringWriter = new StringWriter();

        try {
            marshaller.marshal(object, stringWriter);
        } catch( JAXBException jaxbe ) { 
            throw new SerializationException("Unable to marshall " + object.getClass().getSimpleName() + " instance.", jaxbe);
        }
        String output = stringWriter.toString();

        return output;
    }

    public Object deserialize(Object xmlStrObject) {
        if( ! (xmlStrObject instanceof String) ) { 
            throw new UnsupportedOperationException(JaxbSerializationProvider.class.getSimpleName() + " can only deserialize Strings");
        }
        String xmlStr = (String) xmlStrObject;
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch( JAXBException jaxbe ) { 
            throw new SerializationException("Unable to create unmarshaller.", jaxbe);
        }
        ByteArrayInputStream xmlStrInputStream = new ByteArrayInputStream(xmlStr.getBytes(Charset.forName("UTF-8")));

        Object jaxbObj = null;
        try { 
            jaxbObj = unmarshaller.unmarshal(xmlStrInputStream);
        } catch( JAXBException jaxbe ) { 
           throw new SerializationException("Unable to unmarshal string.", jaxbe);
        }

        return jaxbObj;
    }

    public void addJaxbClasses(Class<?>... jaxbClass) {
        for (int i = 0; i < jaxbClass.length; ++i) {
            jaxbClasses.add(jaxbClass[i]);
            extraJaxbClasses.add(jaxbClass[i]);
        }
        initializeJaxbContext();
    }

    public void addJaxbClasses(Collection<Class<?>> jaxbClassList) {
        for (Class<?> jaxbClass : jaxbClassList) {
            jaxbClasses.add(jaxbClass);
            extraJaxbClasses.add(jaxbClass);
        }
        initializeJaxbContext();
    }

    public Collection<Class<?>> getExtraJaxbClasses() { 
        return new HashSet<Class<?>>(extraJaxbClasses);
    }
    
    public static Set<Class<?>> commaSeperatedStringToClassSet(String extraClassNames) throws SerializationException { 
        return commaSeperatedStringToClassSet(JaxbSerializationProvider.class.getClassLoader(), extraClassNames);
    }
    
    public static Set<Class<?>> commaSeperatedStringToClassSet(ClassLoader classloader, String extraClassNames) throws SerializationException { 
        Set<Class<?>> classList = new HashSet<Class<?>>();
        
        extraClassNames = extraClassNames.trim();
        if( extraClassNames.isEmpty() ) { 
            return classList;
        }
        String [] extraClassNameList = split(extraClassNames);
        if( extraClassNameList.length == 0 ) { 
            return classList;
        }

        // non-empty string/list
        for( String extraClassName : extraClassNameList ) { 
            if( extraClassName.endsWith("[]") ) {
                continue;
            }
            try { 
                classList.add(classloader.loadClass(extraClassName));
            } catch( ClassNotFoundException cnfe ) { 
                throw new SerializationException("Unable to load JAXB class '" + extraClassName, cnfe);
            }
        }
        return classList;
    } 

    public static String classSetToCommaSeperatedString(Collection<Class<?>> extraClassList) throws SerializationException { 
        StringBuilder out = new StringBuilder("");
        Set<Class<?>> extraClassSet = new HashSet<Class<?>>();
        extraClassSet.addAll(extraClassList);
        for( Class<?> extraClass : extraClassSet ) { 
            if (out.length() > 0) {
                out.append(",");
            }
            String extraClassName = extraClass.getCanonicalName();
            if( extraClassName == null ) { 
                throw new SerializationException("Only classes with canonical names can be used for serialization");
            }
            out.append(extraClassName);
        }
        return out.toString();
    }
    
    public static String[] split(String in) {
        String[] splitIn = in.split(",");
        List<String> outList = new ArrayList<String>();
        for (int i = 0; i < splitIn.length; ++i) {
            splitIn[i] = splitIn[i].trim();
            if (!splitIn[i].isEmpty()) {
                outList.add(splitIn[i]);
            }
        }
        return outList.toArray(new String[outList.size()]);
    }

    public void setPrettyPrint(boolean prettyPrint) { 
        this.prettyPrint = prettyPrint;
    }
    
    public boolean getPrettyPrint() { 
        return this.prettyPrint;
    }
}