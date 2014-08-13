package org.kie.services.client.api.command;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyLogicTest {


    protected static Logger logger = LoggerFactory.getLogger(ProxyLogicTest.class);

    @Test
    public void uniqueNameHashCodeSwitchTest() throws Exception { 
        verifyUniqueMethodNameHashCode(KieSession.class);
        verifyUniqueMethodNameHashCode(TaskService.class);
        verifyUniqueMethodNameHashCode(AuditService.class);
    }
    
    private void verifyUniqueMethodNameHashCode(Class interfaceClass) { 
        List<Method> methods = new ArrayList<Method>(Arrays.asList(interfaceClass.getMethods()));
        Collections.sort(methods, new Comparator<Method>() {
            public int compare( Method o1, Method o2 ) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        Map<Integer, String> hashCodeMethodNameMap = new LinkedHashMap<Integer, String>();
        for( Method method : methods ) { 
            String methodName = method.getName();
            String otherMethodName = hashCodeMethodNameMap.get(methodName.hashCode());
            assertTrue( methodName + " does not have a unique hash code (" + otherMethodName + ")", 
                    otherMethodName == null || methodName.equals(otherMethodName));
            hashCodeMethodNameMap.put(methodName.hashCode(), methodName);
        }
        
        for( Entry<Integer, String> entry : hashCodeMethodNameMap.entrySet() ) { 
            String constantName = entry.getValue().replaceAll("(.)([A-Z])", "$1_$2").toUpperCase();
            System.out.println( "private final static int " + constantName + " = " + entry.getKey() + ";" );
        }
        System.out.println( "---" );
        
    }
}
