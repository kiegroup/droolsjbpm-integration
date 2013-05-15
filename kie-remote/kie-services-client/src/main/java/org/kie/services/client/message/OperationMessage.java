package org.kie.services.client.message;

import static org.kie.services.client.message.ServiceMessageMapper.*;
import static org.kie.services.client.message.ServiceMessage.*;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessRuntime;

/**
 * Class containing information for 1 operation (KieSesion or TaskService).
 */
public class OperationMessage {

    private Command command;
    // See ServiceMessage
    private int serviceType;
    // taskId or processInstanceId
    private Long objectId;
    private Object result = null;
    
    public OperationMessage(Command command, int serviceType, Object result) {
        this.command = command;
        this.serviceType = serviceType;
        this.result = result;
    }
    
    public OperationMessage(Command method, Object [] args, int serviceType) { 
        this.command = method;
        this.serviceType = serviceType;
    }

    /**
     * Default constructor for REST operations
     * 
     * @param commandName
     * @param serviceType
     */
    public OperationMessage(String commandName, Object [] args, int serviceType) { 
        // TODO: mapping from commandName/args to command
        // this(getMethodFromNameAndArgs(commandName, args.length, serviceTypeToClass(serviceType)), args);
    }

    /**
     * Constructor when replying to a request
     * 
     * @param operationRequest
     * @param result
     */
    public OperationMessage(OperationMessage operationRequest, Object result) {
        this(operationRequest.command, operationRequest.serviceType, true);
    }

    // Constructor for Request
    public OperationMessage(Command command) { 
        this.command = command;
        // TODO: determine service type (task? kiesession?)
        // setServiceType(command);
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

    public Command getCommand() {
        return this.command;
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

    public Object getResult() {
        return this.result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isResponse() {
        return result != null;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long processInstanceOrTaskId) {
        this.objectId = processInstanceOrTaskId;
    }

}