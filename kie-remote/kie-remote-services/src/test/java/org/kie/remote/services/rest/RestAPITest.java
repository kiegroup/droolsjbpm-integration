package org.kie.remote.services.rest;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.kie.remote.services.rest.api.DeploymentResource;
import org.kie.remote.services.rest.api.DeploymentsResource;
import org.kie.remote.services.rest.api.HistoryResource;
import org.kie.remote.services.rest.api.RuntimeResource;
import org.kie.remote.services.rest.api.TaskResource;

public class RestAPITest {

    private String location;
    
    @Test
    public void testMatchingApi() throws Exception {

        compareInterfaceWithImplementation(DeploymentResource.class, DeploymentResourceImpl.class);
        compareInterfaceWithImplementation(DeploymentsResource.class, DeploymentsResourceImpl.class);
        compareInterfaceWithImplementation(RuntimeResource.class, RuntimeResourceImpl.class);
        compareInterfaceWithImplementation(TaskResource.class, TaskResourceImpl.class);
        compareInterfaceWithImplementation(HistoryResource.class, HistoryResourceImpl.class);
    }

    private void compareInterfaceWithImplementation( Class inter, Class impl ) throws Exception {

        Map<Class, Annotation> implAnnoMap = fillImplAnnoMap(impl.getDeclaredAnnotations());
        for( Annotation interAnno : inter.getDeclaredAnnotations() ) { 
            Annotation implAnno = implAnnoMap.get(interAnno.annotationType());
            assertNotNull(implAnno);
            compareAnnotations(interAnno, implAnno, interAnno.annotationType(), inter.getSimpleName());
        }

        Map<String, Method> implRestMethods = new HashMap<String, Method>();
        for( Method method : impl.getDeclaredMethods() ) {
            implRestMethods.put(method.getName(), method);
        }

        for( Method interRestMethod : inter.getDeclaredMethods() ) {
            Method implRestMethod = implRestMethods.get(interRestMethod.getName());
            assertNotNull(interRestMethod.getName(), implRestMethod);
            implAnnoMap = fillImplAnnoMap(implRestMethod.getDeclaredAnnotations());
            for( Annotation interMethodAnno : interRestMethod.getDeclaredAnnotations()) {
                Annotation implMethodAnno = implAnnoMap.get(interMethodAnno.annotationType());
                assertNotNull(implMethodAnno);
                compareAnnotations(interMethodAnno, implMethodAnno, interMethodAnno.annotationType(), 
                        inter.getSimpleName() + "." + interRestMethod.getName() + " (@" + interMethodAnno.annotationType().getSimpleName());
            }
            Annotation [][] interParamAnnos = interRestMethod.getParameterAnnotations();
            Annotation [][] implParamAnnos = implRestMethod.getParameterAnnotations();
            assertEquals( "Number of parameter annotations: " + interRestMethod.getName(),
                    interParamAnnos.length, implParamAnnos.length);
            for( int i = 0; i < interParamAnnos.length; ++i ) { 
               for( int j = 0; j < interParamAnnos[i].length; ++j ) { 
                   compareAnnotations(interParamAnnos[i][j], implParamAnnos[i][j], interParamAnnos[i][j].annotationType(),
                        inter.getSimpleName() + "." + interRestMethod.getName() + " (param " + i + ")" );
               }
            }
        }
    }

    private Map<Class, Annotation> fillImplAnnoMap( Annotation[] annotations ) {
        Map<Class, Annotation> implAnnoMap = new HashMap<Class, Annotation>();
        for( Annotation anno : annotations ) {
            implAnnoMap.put(anno.annotationType(), anno);
        }
        return implAnnoMap;
    }

    private <T> void compareAnnotations( Annotation inter, Annotation impl, Class<T> type, String where ) throws Exception {
        T interType = type.cast(inter);
        T implType = type.cast(impl);

        Method[] methods = type.getDeclaredMethods();
        for( Method method : methods ) {
            Class returnType = method.getReturnType();
            if( method.getParameterTypes().length > 0 ) {
                continue;
            }
            Object interValue = method.invoke(inter);
            Object implValue = method.invoke(impl);
            assertEquals( where + ": " + type.getSimpleName() + "." + method.getName(), interValue, implValue);
        }
    }
}
