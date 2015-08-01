package org.kie.remote.services.rest.query;

import static org.jbpm.query.jpa.data.QueryParameterIdentifiersUtil.*;
import static org.jbpm.query.jpa.impl.QueryCriteriaUtil.addCriteria;
import static org.kie.internal.query.QueryParameterIdentifiers.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.metamodel.Attribute;

import org.jbpm.process.audit.NodeInstanceLog_;
import org.jbpm.process.audit.ProcessInstanceLog_;
import org.jbpm.process.audit.VariableInstanceLog_;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl_;
import org.jbpm.services.task.impl.model.TaskDataImpl_;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.TaskImpl_;

public abstract class RemoteServicesQueryData {

    // Query Field Info -----------------------------------------------------------------------------------------------------------
    
    final static Map<Class, Map<String, Attribute>> criteriaAttributes 
        = new ConcurrentHashMap<Class, Map<String, Attribute>>();
    private static final AtomicBoolean criteriaAttributesInitialized = new AtomicBoolean(false);

    protected static Map<Class, Map<String, Attribute>> checkAndInitializeCriteriaAttributes() { 
        if( ! criteriaAttributesInitialized.get() ) { 
           if( initializeCriteriaAttributes() ) { 
               criteriaAttributesInitialized.set(true);
           }  else { 
               throw new IllegalStateException("Queries can not be performed if no persistence unit has been initalized!");
           }
        }
        return criteriaAttributes;
    }
    
    protected static synchronized boolean initializeCriteriaAttributes() { 
        if( NodeInstanceLog_.id == null || TaskImpl_.id == null) { 
            // EMF/persistence has not been initialized: 
            // When a persistence unit (EntityManagerFactory) is initialized, 
            // the fields of classes annotated with @StaticMetamodel are filled using reflection
            return false;
        }
        // do not do initialization twice (slow performance, otherwise it doesn't matter)
        if( ! criteriaAttributes.isEmpty() ) { 
           return true; 
        }

        // Audit
        // - ProcessInstanceLog_
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_ID_LIST, ProcessInstanceLog_.processInstanceId);
        addCriteria(criteriaAttributes, PROCESS_ID_LIST, ProcessInstanceLog_.processId);
        addCriteria(criteriaAttributes, START_DATE_LIST, ProcessInstanceLog_.start);
        addCriteria(criteriaAttributes, END_DATE_LIST, ProcessInstanceLog_.end);
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_STATUS_LIST, ProcessInstanceLog_.status);
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_PARENT_ID_LIST, ProcessInstanceLog_.parentProcessInstanceId);
        addCriteria(criteriaAttributes, OUTCOME_LIST, ProcessInstanceLog_.outcome);
        addCriteria(criteriaAttributes, DURATION_LIST, ProcessInstanceLog_.duration);
        addCriteria(criteriaAttributes, IDENTITY_LIST, ProcessInstanceLog_.identity);
        addCriteria(criteriaAttributes, PROCESS_VERSION_LIST, ProcessInstanceLog_.processVersion);
        addCriteria(criteriaAttributes, PROCESS_NAME_LIST, ProcessInstanceLog_.processName);
        addCriteria(criteriaAttributes, CORRELATION_KEY_LIST, ProcessInstanceLog_.correlationKey);
        addCriteria(criteriaAttributes, EXTERNAL_ID_LIST, ProcessInstanceLog_.externalId);
        
        // - NodeInstanceLog
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_ID_LIST, NodeInstanceLog_.processInstanceId);
        addCriteria(criteriaAttributes, PROCESS_ID_LIST, NodeInstanceLog_.processId);
        addCriteria(criteriaAttributes, DATE_LIST, NodeInstanceLog_.externalId);
        addCriteria(criteriaAttributes, EXTERNAL_ID_LIST, NodeInstanceLog_.date);
        
        addCriteria(criteriaAttributes, NODE_INSTANCE_ID_LIST, NodeInstanceLog_.nodeInstanceId);
        addCriteria(criteriaAttributes, NODE_ID_LIST, NodeInstanceLog_.nodeId);
        addCriteria(criteriaAttributes, NODE_NAME_LIST, NodeInstanceLog_.nodeName);
        addCriteria(criteriaAttributes, TYPE_LIST, NodeInstanceLog_.nodeType);
        addCriteria(criteriaAttributes, WORK_ITEM_ID_LIST, NodeInstanceLog_.workItemId);
        
        // - VariableInstanceLog
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_ID_LIST, VariableInstanceLog_.processInstanceId);
        addCriteria(criteriaAttributes, PROCESS_ID_LIST, VariableInstanceLog_.processId);
        addCriteria(criteriaAttributes, DATE_LIST, VariableInstanceLog_.date);
        addCriteria(criteriaAttributes, EXTERNAL_ID_LIST, VariableInstanceLog_.externalId);
        
        addCriteria(criteriaAttributes, VARIABLE_INSTANCE_ID_LIST, VariableInstanceLog_.variableInstanceId);
        addCriteria(criteriaAttributes, VARIABLE_ID_LIST, VariableInstanceLog_.variableId);
        addCriteria(criteriaAttributes, VALUE_LIST, VariableInstanceLog_.value);
        addCriteria(criteriaAttributes, OLD_VALUE_LIST, VariableInstanceLog_.oldValue);
        
        // TASK
        addCriteria(criteriaAttributes, TASK_ACTIVATION_TIME_LIST,  TaskImpl.class, TaskDataImpl_.activationTime);
        addCriteria(criteriaAttributes, ARCHIVED,                   TaskImpl_.archived);
        addCriteria(criteriaAttributes, CREATED_ON_LIST,            TaskImpl.class, TaskDataImpl_.createdOn);
        addCriteria(criteriaAttributes, EXTERNAL_ID_LIST,           TaskImpl.class, TaskDataImpl_.deploymentId);
        addCriteria(criteriaAttributes, EXPIRATION_TIME_LIST,       TaskImpl.class, TaskDataImpl_.expirationTime);
        addCriteria(criteriaAttributes, TASK_FORM_NAME_LIST,        TaskImpl_.formName);
        addCriteria(criteriaAttributes, PROCESS_ID_LIST,            TaskImpl.class, TaskDataImpl_.processId); 
        addCriteria(criteriaAttributes, PROCESS_INSTANCE_ID_LIST,   TaskImpl.class, TaskDataImpl_.processInstanceId); 
        addCriteria(criteriaAttributes, PROCESS_SESSION_ID_LIST,    TaskImpl.class, TaskDataImpl_.processSessionId); 
        addCriteria(criteriaAttributes, SKIPPABLE,                  TaskImpl.class, TaskDataImpl_.skipable); 
        addCriteria(criteriaAttributes, TASK_STATUS_LIST,           TaskImpl.class, TaskDataImpl_.status);
        addCriteria(criteriaAttributes, SUB_TASKS_STRATEGY,         TaskImpl_.subTaskStrategy);
        addCriteria(criteriaAttributes, TASK_ID_LIST,               TaskImpl_.id);
        addCriteria(criteriaAttributes, TASK_PARENT_ID_LIST,        TaskImpl.class, TaskDataImpl_.parentId);
        addCriteria(criteriaAttributes, TYPE_LIST,                  TaskImpl_.taskType);
        addCriteria(criteriaAttributes, WORK_ITEM_ID_LIST,          TaskImpl.class, TaskDataImpl_.workItemId);
        addCriteria(criteriaAttributes, TASK_PRIORITY_LIST,         TaskImpl.class, TaskImpl_.priority);
        
        addCriteria(criteriaAttributes, TASK_DESCRIPTION_LIST,      TaskImpl_.descriptions);
        addCriteria(criteriaAttributes, TASK_NAME_LIST,             TaskImpl_.names);
        addCriteria(criteriaAttributes, TASK_SUBJECT_LIST,          TaskImpl_.subjects);
        
        addCriteria(criteriaAttributes, ACTUAL_OWNER_ID_LIST,       TaskImpl.class, TaskDataImpl_.actualOwner);
        addCriteria(criteriaAttributes, CREATED_BY_LIST,            TaskImpl.class, TaskDataImpl_.createdBy); // initiator
        
        addCriteria(criteriaAttributes, BUSINESS_ADMIN_ID_LIST,     TaskImpl.class, PeopleAssignmentsImpl_.businessAdministrators);
        addCriteria(criteriaAttributes, POTENTIAL_OWNER_ID_LIST,    TaskImpl.class, PeopleAssignmentsImpl_.potentialOwners);
        addCriteria(criteriaAttributes, STAKEHOLDER_ID_LIST,        TaskImpl.class, PeopleAssignmentsImpl_.taskStakeholders);
        addCriteria(criteriaAttributes, EXCLUDED_OWNER_ID_LIST,     TaskImpl.class, PeopleAssignmentsImpl_.excludedOwners);
        
        return true;
    }
 
    public final static Set<String> procInstLogNeededCriterias = new CopyOnWriteArraySet<String>();
    public final static Set<String> varInstLogNeededCriterias = new CopyOnWriteArraySet<String>();
    public final static Set<String> taskNeededCriterias = new CopyOnWriteArraySet<String>();
    
    public final static Set<String> taskSpecificCriterias = new CopyOnWriteArraySet<String>();
    public final static Set<String> varInstLogSpecificCriterias = new CopyOnWriteArraySet<String>();
    
    static { 
      
        // when doing one of the following queries: 
        // - TaskImpl 
        // - ProcessInstanceLog 
        // then add 
        // + [VariableInstanceLog] 
        // if these criteria are used
        varInstLogNeededCriterias.add(VARIABLE_ID_LIST);
        varInstLogNeededCriterias.add(VALUE_LIST);
        varInstLogNeededCriterias.add(OLD_VALUE_LIST);
        varInstLogNeededCriterias.add(EXTERNAL_ID_LIST);
        varInstLogNeededCriterias.add(VARIABLE_INSTANCE_ID_LIST);
        
        varInstLogSpecificCriterias.add(VAR_VALUE_ID_LIST);
        varInstLogSpecificCriterias.add(LAST_VARIABLE_LIST);
        
        // when doing one of the following queries: 
        // - VariableInstanceLog
        // - ProcessInstanceLog
        //  
        // then add 
        // + [TaskImpl] or [TaskDataImpl] 
        // if these criteria are used
        taskNeededCriterias.add(TASK_ID_LIST);
        taskNeededCriterias.add(TASK_STATUS_LIST);
        taskNeededCriterias.add(CREATED_BY_LIST);
        taskNeededCriterias.add(STAKEHOLDER_ID_LIST);
        taskNeededCriterias.add(POTENTIAL_OWNER_ID_LIST);
        taskNeededCriterias.add(ACTUAL_OWNER_ID_LIST);
        taskNeededCriterias.add(BUSINESS_ADMIN_ID_LIST);
        taskNeededCriterias.add(WORK_ITEM_ID_LIST);
        
        taskNeededCriterias.add(STAKEHOLDER_ID_LIST);
        taskNeededCriterias.add(POTENTIAL_OWNER_ID_LIST);
        taskNeededCriterias.add(BUSINESS_ADMIN_ID_LIST);
        
        taskSpecificCriterias.add(TASK_USER_ROLES_LIMIT_LIST);
        
        // when doing one of the following queries: 
        // - TaskImpl 
        // - VariableInstanceLog
        // 
        // then add 
        // + [ProcessInstanceLog] 
        // if these criteria are used
        procInstLogNeededCriterias.add(START_DATE_LIST);
        procInstLogNeededCriterias.add(END_DATE_LIST);
        procInstLogNeededCriterias.add(PROCESS_INSTANCE_STATUS_LIST);
        procInstLogNeededCriterias.add(PROCESS_VERSION_LIST);
    }
}
