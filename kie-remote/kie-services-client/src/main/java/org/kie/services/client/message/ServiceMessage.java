package org.kie.services.client.message;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;

public class ServiceMessage {

    /**
     * Indicates that an {@link OperationMessage} references a {@link KieSession} operation
     */
    public static transient final int KIE_SESSION_REQUEST = 0;
    /**
     * Indicates that an {@link OperationMessage} references a {@link TaskService} operation
     */
    public static transient final int TASK_SERVICE_REQUEST = 1;
    /**
     * Indicates that an {@link OperationMessage} references a {@link WorkItemManager} operation
     */
    public static transient final int WORK_ITEM_MANAGER_REQUEST = 2;

    private final static int version = 1;
    private String domainName;
    
    private List<OperationMessage> operations = new ArrayList<OperationMessage>();

    public ServiceMessage() { 
        // default constructor
    }
    
    public ServiceMessage(String domainName) { 
        this.domainName = domainName;
    }
    
    public List<OperationMessage> getOperations() {
        return operations;
    }

    public int getVersion() {
        return version;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void addOperation(OperationMessage operation) {
        if( operation != null ) { 
            operations.add(operation);
        }
    }
    
}
