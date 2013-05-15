package org.kie.services.client.message;

import static org.kie.services.client.message.ServiceMessage.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.ws.rs.core.MultivaluedMap;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;

public class ServiceMessageMapper {

    public static int serviceClassToType(Class<?> serviceClass) {
        if (serviceClass.getSimpleName().toLowerCase().matches(".*kiesession.*")) {
            return ServiceMessage.KIE_SESSION_REQUEST;
        } else if (serviceClass.getSimpleName().toLowerCase().matches(".*taskservice.*")) {
            return ServiceMessage.TASK_SERVICE_REQUEST;
        } else if (serviceClass.getSimpleName().toLowerCase().matches(".*workitemmanager.*")) {
            return ServiceMessage.WORK_ITEM_MANAGER_REQUEST;
        } else {
            return -1;
        }
    }

    public static Class<?> serviceTypeToClass(int serviceType) {
        switch (serviceType) {
        case KIE_SESSION_REQUEST:
            return KieSession.class;
        case WORK_ITEM_MANAGER_REQUEST:
            return WorkItemManager.class;
        case TASK_SERVICE_REQUEST:
            return TaskService.class;
        default:
            throw new UnsupportedOperationException("Unknown service type: " + serviceType);
        }
    }

    /**
     * See {@link #getMethodFromNameAndArgs(String, int, Class)}
     */
    public static Command getMethodFromNameAndArgs(String methodName, int numArgs, int serviceType) {
        Class<?> service = serviceTypeToClass(serviceType);
        return getMethodFromNameAndArgs(methodName, numArgs, service);
    }
    

    public static Command getMethodFromNameAndArgs(String methodName, int numArgs, Class<?> service) {
        return null;
    }

   // REST query mapping
    
    public static ServiceMessage convertQueryParamsToServiceMsg(String domain, String operName, MultivaluedMap<String, String> queryParams, int serviceType, Long objectId, 
            Map<Method, Map<String, Integer>> methodArgsMap) {
        Command method = getMethodFromNameAndArgs(operName, queryParams.size(), serviceType);
        Object [] args = convertQueryParamsToMethodParams(method, queryParams, methodArgsMap);
        ServiceMessage srvcMsg = new ServiceMessage(domain);
        OperationMessage operMsg = new OperationMessage(method, serviceType);
        srvcMsg.addOperation(operMsg);
        return srvcMsg;
    }
    
    /**
     * If an operation has a Map<String, Object> as a param, then the user can pass that information as follows in a REST url: 
     * 
     * For example, the following: 
     * 
     * .../startProcess?processId=evaluation?map_varOne=spam&map_varTwo=bacon
     * 
     * would translate to calling the "startProcess" method/operation/command 
     * with the following params: 
     * 
     *   String processId = "evaluation"
     *   Map<String, Object> params = { 
     *      "varOne" => "spam",
     *      "varTwo" => "bacon"
     *   }
     *   
     * This does mean it's not possible to pass anythign else than a String as a parameter since passing type info
     * would complicate this api significantly
     */
    private static Object [] convertQueryParamsToMethodParams(Method method, MultivaluedMap<String, String> queryParams, Map<Method, Map<String, Integer>> methodArgsMap) { 
       Map<String, Integer> argsMap = methodArgsMap.get(method);
       Type [] methodTypes = method.getParameterTypes();
       Object [] args = new Object[method.getParameterTypes().length];
       Map<String, Object> operParams = null;
       for( String key : queryParams.keySet() ) { 
           if( key.startsWith("map_") ) { 
               // TODO: put in Map<String, Object> params and pass as param to oper
               continue;
           }
           
          Integer index = argsMap.get(key); 
          if( index == null ) { 
              throw new UnsupportedOperationException("Unknown query parameter " + key + " for operation " + method.getName() );
          }
          Object arg = null;
          int numValues = queryParams.get(key).size(); 
          if( numValues != 1 ) {
              throw new UnsupportedOperationException("Query parameter " + key + " for operation " + method.getName() + " may only occur once, not " + numValues + " times." );
          }
          if( Object.class.equals(methodTypes[index])) {
          } else if( String.class.equals(methodTypes[index]) ) { 
              args[index] = queryParams.getFirst(key);
          } else if( Object.class.equals(methodTypes[index]) ) { 
              args[index] = queryParams.getFirst(key);
          } else if( long.class.equals(methodTypes[index]) ) { 
              String numStr = queryParams.getFirst(key);
              try { 
                  args[index] = Long.valueOf(numStr);
              } catch( NumberFormatException nfe ) {
                  throw new IllegalStateException("Query parameter " + key + " contained a non-numerical value: '" + numStr + "'.");
              }
          } else if( boolean.class.equals(methodTypes[index]) ) { 
              String boolStr = queryParams.getFirst(key);
              try { 
                  args[index] = Boolean.valueOf(boolStr);
              } catch( NumberFormatException nfe ) {
                  throw new IllegalStateException("Query parameter " + key + " contained a non-boolean value: '" + boolStr + "'.");
              }
          } else { 
             throw new UnsupportedOperationException( "Argument handling for " + method.getName() + " is not yet supported.");
          }
          
          args[index] = arg;
       }

       return args;
    }
    
}
