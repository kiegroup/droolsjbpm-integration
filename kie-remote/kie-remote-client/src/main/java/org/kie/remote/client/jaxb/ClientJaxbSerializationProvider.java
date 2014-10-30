package org.kie.remote.client.jaxb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationException;

public class ClientJaxbSerializationProvider extends JaxbSerializationProvider {

    private static Set<Class<?>> CLIENT_SIDE_JAXB_CLASS_SET;
    static { 
        Class [] serviceSideClasses = { 
                JaxbCommandsRequest.class,
                JaxbCommandsResponse.class,
                JaxbContentResponse.class,
                JaxbTaskResponse.class,
                JaxbTaskSummaryListResponse.class
        };
        List<Class<?>> serverSideJaxbClassList = new ArrayList<Class<?>>();
        for( Class clazz : serviceSideClasses ) { 
            serverSideJaxbClassList.add(clazz);
        } 
        CLIENT_SIDE_JAXB_CLASS_SET = Collections.unmodifiableSet(new HashSet<Class<?>>(serverSideJaxbClassList));
    }
    
    public static Set<Class<?>> getModuleClasses() {
        return CLIENT_SIDE_JAXB_CLASS_SET;
    }
    
  // General methods -------------------------------------------------------------------------------------------------------------------
    
    private static Class<?> [] ALL_BASE_JAXB_CLASSES = null;
    static { 
        int kieJaxbClassSetLength = KIE_JAXB_CLASS_SET.size();
        Class<?> [] types = new Class<?> [kieJaxbClassSetLength + PRIMITIVE_ARRAY_CLASS_SET.size()];
        System.arraycopy(KIE_JAXB_CLASS_SET.toArray(new Class<?>[kieJaxbClassSetLength]), 0, types, 0, kieJaxbClassSetLength);
        int primArrClassSetLength = PRIMITIVE_ARRAY_CLASS_SET.size();
        System.arraycopy(PRIMITIVE_ARRAY_CLASS_SET.toArray(new Class<?>[primArrClassSetLength]), 0, types, kieJaxbClassSetLength, primArrClassSetLength);
        ALL_BASE_JAXB_CLASSES = types;
    }

    private Class<?> [] getAllJaxbClasses() { 
        Class<?> [] allBaseJaxbClassArr = getAllBaseJaxbClasses();
        if( extraJaxbClasses.isEmpty() ) { 
            return allBaseJaxbClassArr;
        }
        Class<?> [] extraJaxbClassArr = extraJaxbClasses.toArray(new Class<?>[extraJaxbClasses.size()]);
        return addClassArrToClassArr(allBaseJaxbClassArr, extraJaxbClassArr);
    }
    
    public static Class<?> [] getAllBaseJaxbClasses() { 
        Set<Class<?>> sideJaxbClassSet = getModuleClasses();
        Class<?> [] sideJaxbClasses = new Class<?>[sideJaxbClassSet.size()];
        sideJaxbClasses = sideJaxbClassSet.toArray(sideJaxbClasses);
        return addClassArrToClassArr(ALL_BASE_JAXB_CLASSES, sideJaxbClasses);
    }

    private static Class<?> [] addClassArrToClassArr(Class<?> [] baseArr, Class<?> [] addArr) { 
        Class<?> [] copy = new Class<?>[baseArr.length + addArr.length];
        System.arraycopy(baseArr, 0, copy, 0, baseArr.length);
        System.arraycopy(addArr, 0, copy, baseArr.length, addArr.length);
        return copy;
    }
    
    private JAXBContext jaxbContext = null;
    protected Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();

    public JAXBContext getJaxbContext() { 
        return jaxbContext;
    }
    
    public static JaxbSerializationProvider newInstance() { 
        ClientJaxbSerializationProvider jaxbSerProvider = new ClientJaxbSerializationProvider();
        jaxbSerProvider.initialize();
        return jaxbSerProvider;
    }
    
    public static JaxbSerializationProvider newInstance(Collection<Class<?>> extraJaxbClassList) { 
        ClientJaxbSerializationProvider jaxbSerProvider = new ClientJaxbSerializationProvider(extraJaxbClassList);
        jaxbSerProvider.initialize();
        return jaxbSerProvider;
    }
   
    private void initialize() {
        initialize(getAllJaxbClasses());
    }

    private void initialize(Class<?> [] jaxbClasses) {
        try {
            this.jaxbContext = JAXBContext.newInstance(jaxbClasses);
        } catch (JAXBException jaxbe) {
            throw new SerializationException("Unsupported JAXB Class encountered during initialization: " + jaxbe.getMessage(), jaxbe);
        }
    }
   
    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.JaxbSerializationProvider#dispose()
     */
    @Override
    public void dispose() { 
       if( this.extraJaxbClasses != null ) { 
           this.extraJaxbClasses.clear();
           this.extraJaxbClasses = null;
       }
       if( this.jaxbContext != null ) { 
           this.jaxbContext = null;
       }
    }
  
    //  Functional methods -------------------------------------------------------------------------------------------------------
 
    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.JaxbSerializationProvider#addJaxbClasses(java.lang.Class)
     */
    @Override
    public void addJaxbClasses(Class... jaxbClass) {
        for (int i = 0; i < jaxbClass.length; ++i) {
            extraJaxbClasses.add(jaxbClass[i]);
        }
    }

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.JaxbSerializationProvider#addJaxbClassesAndInitialize(java.lang.Class)
     */
    @Override
    public void addJaxbClassesAndReinitialize(Class... jaxbClass) {
        addJaxbClasses(jaxbClass);
        initialize(getAllJaxbClasses());
    }

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.JaxbSerializationProvider#getExtraJaxbClasses()
     */
    @Override
    public Collection<Class<?>> getExtraJaxbClasses() { 
        return new HashSet<Class<?>>(extraJaxbClasses);
    }
  
    // Constructors ---------------------------------------------------------------------------------------------------------------

    private ClientJaxbSerializationProvider() { 
        // default 
    }
    
    private ClientJaxbSerializationProvider(Collection<Class<?>> extraJaxbClassArr) { 
        this.extraJaxbClasses.addAll(extraJaxbClassArr);
    }
    
    private ClientJaxbSerializationProvider(Class<?> [] extraJaxbClassArr) { 
        this.extraJaxbClasses.addAll(Arrays.asList(extraJaxbClassArr));
    }

}
