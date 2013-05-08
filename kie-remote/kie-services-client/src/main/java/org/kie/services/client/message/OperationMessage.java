package org.kie.services.client.message;

import static org.kie.services.client.message.ServiceMessageMapper.*;
import static org.kie.services.client.message.ServiceMessage.*;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessRuntime;

/**
 * Class containing information for 1 operation (KieSesion or TaskService).
 */
public class OperationMessage {

    private Method method;
    // See ServiceMessage
    private int serviceType;
    // taskId or processInstanceId
    private Long objectId;
    private Object[] args;
    private boolean response = false;
    
    public OperationMessage(Method method, int serviceType, boolean response) {
        this.method = method;
        this.serviceType = serviceType;
        this.response = response;
    }
    
    public OperationMessage(Method method, Object [] args, int serviceType) { 
        this.method = method;
        this.serviceType = serviceType;
        this.args = args;
    }

    /**
     * Default constructor for REST operations
     * 
     * @param methodName
     * @param serviceType
     */
    public OperationMessage(String methodName, Object [] args, int serviceType) { 
        this(getMethodFromNameAndArgs(methodName, args.length, serviceTypeToClass(serviceType)), args);
    }

    /**
     * Constructor when replying to a request
     * 
     * @param operationRequest
     * @param result
     */
    public OperationMessage(OperationMessage operationRequest, Object result) {
        this(operationRequest.method, operationRequest.serviceType, true);
        this.args = new Object[1];
        this.args[0] = result;
    }

    // Constructor for Request
    public OperationMessage(Method method, Object[] args) {
        this.method = method;
        this.args = args;
        setServiceType(method);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setServiceType(Method method) {
        Class<?> serviceClass = method.getDeclaringClass();
        this.serviceType = serviceClassToType(serviceClass);

        if (this.serviceType == -1) {
            // Only necessary if we use the "Same" API

            Queue<Class<?>> interfaces = new LinkedList(Arrays.asList(KieSession.class.getInterfaces()));
            while (!interfaces.isEmpty()) {
                Class<?> inter = interfaces.poll();
                if (serviceClass.equals(inter)) {
                    this.serviceType = ServiceMessage.KIE_SESSION_REQUEST;
                    return;
                }
                interfaces.addAll(Arrays.asList(inter.getInterfaces()));
            }

            // TaskService does not extend (nor is extended by) any other classes

            throw new UnsupportedOperationException("Unsupported service class: " + serviceClass.getCanonicalName());
        }
    }

    public Method getMethod() {
        return this.method;
    }

    public String getMethodName() {
        return this.method.getName();
    }

    /**
     * See {@link ServiceMessage#KIE_SESSION_REQUEST}, {@link ServiceMessage#TASK_SERVICE_REQUEST},
     * {@link ServiceMessage#WORK_ITEM_MANAGER_REQUEST}
     * 
     * @return
     */
    public int getServiceType() {
        return this.serviceType;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getResult() {
        return this.args[0];
    }

    public void setResult(Object result) {
        this.args = new Object[1];
        this.args[0] = result;
        this.response = true;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean responseStatus) {
        this.response = responseStatus;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long processInstanceOrTaskId) {
        this.objectId = processInstanceOrTaskId;
    }

    // helper methods (methodName --> method) ------------------------------------------------------------------------------------

}