package org.kie.services.client.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.kie.services.client.serialization.jaxb.impl.JaxbRestRequestException;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPrimitiveResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.JaxbStringListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceFormResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItemResponse;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskContentResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskFormResponse;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public class JaxbSerializationProvider implements SerializationProvider {

    // Classes -------------------------------------------------------------------------------------------------------------------
    
    public final static int JMS_SERIALIZATION_TYPE = 0;

    public static Set<Class<?>> KIE_JAXB_CLASS_SET;
    static { 
        Class<?> [] kieJaxbClasses = { 
                // command response
                JaxbTaskContentResponse.class,
                JaxbTaskFormResponse.class,
                JaxbProcessInstanceListResponse.class,
                JaxbProcessInstanceResponse.class,
                JaxbProcessInstanceWithVariablesResponse.class,
                JaxbProcessInstanceFormResponse.class,

                // REST other
                JaxbGenericResponse.class,
                JaxbLongListResponse.class,
                JaxbStringListResponse.class,
                JaxbOtherResponse.class,
                JaxbPrimitiveResponse.class,
                JaxbVariablesResponse.class,
                JaxbExceptionResponse.class,
                JaxbGenericResponse.class,
                JaxbRequestStatus.class,

                // deployment
                JaxbDeploymentJobResult.class,
                JaxbDeploymentUnit.class,
                JaxbDeploymentUnitList.class,
                JaxbDeploymentDescriptor.class,
                
                // process
                JaxbProcessDefinition.class,
                JaxbProcessDefinitionList.class,

                // workitem
                JaxbWorkItemResponse.class,

                // history
                JaxbHistoryLogList.class,
                JaxbNodeInstanceLog.class,
                JaxbProcessInstanceLog.class,
                JaxbVariableInstanceLog.class,
                
                // task
                JaxbTaskSummary.class,
               
                // query
                JaxbQueryTaskResult.class,
                JaxbQueryProcessInstanceResult.class,
                
                // exception
                JaxbRestRequestException.class
        };
        KIE_JAXB_CLASS_SET = new CopyOnWriteArraySet<Class<?>>(Arrays.asList(kieJaxbClasses));
    }
    
    private static Set<Class<?>> CLIENT_SIDE_JAXB_CLASS_SET;
    static { 
        String [] clientSideClasses = { 
                "org.kie.remote.client.jaxb.JaxbCommandsRequest",
                "org.kie.remote.client.jaxb.JaxbCommandsResponse",
                "org.kie.remote.client.jaxb.JaxbContentResponse",
                "org.kie.remote.client.jaxb.JaxbTaskResponse",
                "org.kie.remote.client.jaxb.JaxbTaskSummaryListResponse"
        };
        
       
        List<Class<?>> clientSideJaxbClassList = new ArrayList<Class<?>>();
        try { 
            addClassesToList(clientSideClasses, clientSideJaxbClassList);
        } catch( ClassNotFoundException cnfe ) { 
                // do nothing
        }
        CLIENT_SIDE_JAXB_CLASS_SET = Collections.unmodifiableSet(new HashSet<Class<?>>(clientSideJaxbClassList));
    };
    
    private static Set<Class<?>> SERVER_SIDE_JAXB_CLASS_SET;
    static { 
        String [] serviceSideClasses = { 
                "org.kie.remote.services.jaxb.JaxbCommandsRequest",
                "org.kie.remote.services.jaxb.JaxbCommandsResponse",
                "org.kie.remote.services.jaxb.JaxbContentResponse",
                "org.kie.remote.services.jaxb.JaxbTaskResponse",
                "org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse"
        };
        List<Class<?>> serverSideJaxbClassList = new ArrayList<Class<?>>();
        try { 
            addClassesToList(serviceSideClasses, serverSideJaxbClassList);
        } catch( ClassNotFoundException clientCnfe ) { 
            // do nothing
        }
        SERVER_SIDE_JAXB_CLASS_SET = Collections.unmodifiableSet(new HashSet<Class<?>>(serverSideJaxbClassList));
    }

    private static void addClassesToList(String [] classes, List<Class<?>> list) throws ClassNotFoundException { 
        for( int i = 0; i < classes.length; ++i ) { 
            Class moduleDependentClass = Class.forName(classes[i]);
            list.add(moduleDependentClass);
        }
    }
    
    public static Set<Class<?>> PRIMITIVE_ARRAY_CLASS_SET;
    static { 
        Class<?> [] primitiveClasses = { 
                new Boolean[]{}.getClass(),
                new Byte[]{}.getClass(),
                new Character[]{}.getClass(),
                new Double[]{}.getClass(),
                new Float[]{}.getClass(),
                new Integer[]{}.getClass(),
                new Long[]{}.getClass(),
                new Math[]{}.getClass(),
                new Number[]{}.getClass(),
                new Short[]{}.getClass(),
                new String[]{}.getClass()
        };
        PRIMITIVE_ARRAY_CLASS_SET = new CopyOnWriteArraySet<Class<?>>(Arrays.asList(primitiveClasses));
    };

    private static Class<?> [] ALL_BASE_JAXB_CLASSES = null;
    static { 
        int kieJaxbClassSetLength = KIE_JAXB_CLASS_SET.size();
        Class<?> [] types = new Class<?> [kieJaxbClassSetLength + PRIMITIVE_ARRAY_CLASS_SET.size()];
        System.arraycopy(KIE_JAXB_CLASS_SET.toArray(new Class<?>[kieJaxbClassSetLength]), 0, types, 0, kieJaxbClassSetLength);
        int primArrClassSetLength = PRIMITIVE_ARRAY_CLASS_SET.size();
        System.arraycopy(PRIMITIVE_ARRAY_CLASS_SET.toArray(new Class<?>[primArrClassSetLength]), 0, types, kieJaxbClassSetLength, primArrClassSetLength);
        ALL_BASE_JAXB_CLASSES = types;
    }
            
    public static Class<?> [] getAllBaseJaxbClasses(boolean clientSide) { 
        Set<Class<?>> sideJaxbClassSet = clientSide ? CLIENT_SIDE_JAXB_CLASS_SET : SERVER_SIDE_JAXB_CLASS_SET;
        Class<?> [] sideJaxbClasses = new Class<?>[sideJaxbClassSet.size()];
        sideJaxbClasses = sideJaxbClassSet.toArray(sideJaxbClasses);
        Class<?> [] copy = new Class<?>[ALL_BASE_JAXB_CLASSES.length + sideJaxbClasses.length];
        System.arraycopy(ALL_BASE_JAXB_CLASSES, 0, copy, 0, ALL_BASE_JAXB_CLASSES.length);
        System.arraycopy(sideJaxbClasses, 0, copy, ALL_BASE_JAXB_CLASSES.length, sideJaxbClasses.length);
        return copy;
    }

    // Local/instance methods ----------------------------------------------------------------------------------------------------
    
    private boolean prettyPrint = false;
    private JAXBContext jaxbContext = null;
    private Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
 
    public static JaxbSerializationProvider clientSideInstance() { 
        return new JaxbSerializationProvider(Arrays.asList(getAllBaseJaxbClasses(true)));
    }
    
    public static JaxbSerializationProvider clientSideInstance(Class<?>... extraJaxbClasses) { 
        HashSet<Class<?>> jaxbClasses = new HashSet<Class<?>>(Arrays.asList(getAllBaseJaxbClasses(true)));
        jaxbClasses.addAll(Arrays.asList(extraJaxbClasses));
        return new JaxbSerializationProvider(jaxbClasses);
    }
    
    public static JaxbSerializationProvider clientSideInstance(Collection<Class<?>> extraJaxbClassList) { 
        HashSet<Class<?>> jaxbClasses = new HashSet<Class<?>>(Arrays.asList(getAllBaseJaxbClasses(true)));
        jaxbClasses.addAll(extraJaxbClassList);
        return new JaxbSerializationProvider(jaxbClasses);
    }
   
    public static JaxbSerializationProvider serverSideInstance() { 
        return new JaxbSerializationProvider(Arrays.asList(getAllBaseJaxbClasses(false)));
    }
    
    public static JaxbSerializationProvider newInstance(JAXBContext jaxbContext) { 
         return new JaxbSerializationProvider(jaxbContext);
    }
    
    private JaxbSerializationProvider(Collection<Class<?>> classList) {
        initializeJaxbContexts(classList.toArray(new Class<?>[classList.size()]));
    }
   
    private JaxbSerializationProvider(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    private void initializeJaxbContexts(Class<?> [] jaxbClasses) {
        try {
            jaxbContext = JAXBContext.newInstance(jaxbClasses);
        } catch (JAXBException jaxbe) {
            throw new SerializationException("Unsupported JAXB Class encountered during initialization: " + jaxbe.getMessage(), jaxbe);
        }
    }
    
    public void dispose() { 
       if( this.extraJaxbClasses != null ) { 
           this.extraJaxbClasses.clear();
           this.extraJaxbClasses = null;
       }
       if( this.jaxbContext != null ) { 
           this.jaxbContext = null;
       }
    }
   
    public void setPrettyPrint(boolean prettyPrint) { 
        this.prettyPrint = prettyPrint;
    }

    public boolean getPrettyPrint() { 
        return this.prettyPrint;
    }

    public synchronized String serialize(Object object) {
        return serialize(jaxbContext, prettyPrint, object);
    }
    
    public static String serialize(JAXBContext jaxbContext, boolean prettyPrint, Object object) {
        Marshaller marshaller = null;
        try { 
            marshaller = jaxbContext.createMarshaller();
            if( prettyPrint ) { 
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }
        } catch( JAXBException jaxbe ) { 
            throw new SerializationException("Unable to create JAXB marshaller", jaxbe);
        }
        
        try {
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
                public void escape(char[] ac, int i, int j, boolean flag, Writer writer) throws IOException {
                    writer.write( ac, i, j ); 
                }
            });
        } catch (PropertyException e) {
            throw new SerializationException("Unable to set CharacterEscapeHandler", e);
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

    public synchronized Object deserialize(String xmlStr) {
       return deserialize(jaxbContext, xmlStr);
    }
    
    public static Object deserialize(JAXBContext jaxbContext, String xmlStr) {
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

    public void addJaxbClasses(boolean clientSide, Class... jaxbClass) {
        for (int i = 0; i < jaxbClass.length; ++i) {
            extraJaxbClasses.add(jaxbClass[i]);
        }
        Set<Class<?>> jaxbClassSet = new HashSet<Class<?>>(extraJaxbClasses); 
        jaxbClassSet.addAll(Arrays.asList(getAllBaseJaxbClasses(clientSide)));
        initializeJaxbContexts(jaxbClassSet.toArray(new Class<?>[jaxbClassSet.size()]));
    }

    public Collection<Class<?>> getExtraJaxbClasses() { 
        return new HashSet<Class<?>>(extraJaxbClasses);
    }
   
    // methods for class set properties (JMS messages) ----------------------------------------------------------------------------
    
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
    
    static String[] split(String in) {
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

    public static <T> T unsupported(Class<?> realClass, Class<T> returnType) { 
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + realClass.getSimpleName() + " implementation.");
    }

}