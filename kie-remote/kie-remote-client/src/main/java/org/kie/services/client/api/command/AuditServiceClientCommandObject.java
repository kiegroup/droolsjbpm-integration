package org.kie.services.client.api.command;

import java.util.List;

import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.remote.jaxb.gen.ClearHistoryLogsCommand;
import org.kie.remote.jaxb.gen.FindActiveProcessInstancesCommand;
import org.kie.remote.jaxb.gen.FindNodeInstancesCommand;
import org.kie.remote.jaxb.gen.FindProcessInstanceCommand;
import org.kie.remote.jaxb.gen.FindProcessInstancesCommand;
import org.kie.remote.jaxb.gen.FindSubProcessInstancesCommand;
import org.kie.remote.jaxb.gen.FindVariableInstancesByNameCommand;
import org.kie.remote.jaxb.gen.FindVariableInstancesCommand;

public class AuditServiceClientCommandObject extends AbstractRemoteCommandObject implements AuditService {

    public AuditServiceClientCommandObject(RemoteConfiguration config) {
        super(config);
    }
    
    @Override
    public List<? extends ProcessInstanceLog> findProcessInstances() {
        return (List<ProcessInstanceLog>) executeCommand(new FindProcessInstancesCommand());
    }

    @Override
    public List<? extends ProcessInstanceLog> findProcessInstances( String processId ) {
        FindProcessInstancesCommand cmd = new FindProcessInstancesCommand();
        cmd.setProcessId(processId);
        return (List<ProcessInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends ProcessInstanceLog> findActiveProcessInstances( String processId ) {
        FindActiveProcessInstancesCommand cmd = new FindActiveProcessInstancesCommand();
        cmd.setProcessId(processId);
        return (List<ProcessInstanceLog>) executeCommand(cmd);
    }

    @Override
    public ProcessInstanceLog findProcessInstance( long processInstanceId ) {
        FindProcessInstanceCommand cmd = new FindProcessInstanceCommand();
        cmd.setProcessInstanceId(processInstanceId);
        return (ProcessInstanceLog) executeCommand(cmd);
    }

    @Override
    public List<? extends ProcessInstanceLog> findSubProcessInstances( long processInstanceId ) {
        FindSubProcessInstancesCommand cmd = new FindSubProcessInstancesCommand();
        cmd.setProcessInstanceId(processInstanceId);
        return (List<ProcessInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends NodeInstanceLog> findNodeInstances( long processInstanceId ) {
        FindNodeInstancesCommand cmd = new FindNodeInstancesCommand();
        cmd.setProcessInstanceId(processInstanceId);
        return (List<NodeInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends NodeInstanceLog> findNodeInstances( long processInstanceId, String nodeId ) {
        FindNodeInstancesCommand cmd = new FindNodeInstancesCommand();
        cmd.setProcessInstanceId(processInstanceId);
        cmd.setNodeId(nodeId);
        return (List<NodeInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends VariableInstanceLog> findVariableInstances( long processInstanceId ) {
        FindVariableInstancesCommand cmd = new FindVariableInstancesCommand();
        cmd.setProcessInstanceId(processInstanceId);
        return (List<VariableInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends VariableInstanceLog> findVariableInstances( long processInstanceId, String variableId ) {
        FindVariableInstancesCommand cmd = new FindVariableInstancesCommand();
        cmd.setProcessInstanceId(processInstanceId);
        cmd.setVariableId(variableId);
        return (List<VariableInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends VariableInstanceLog> findVariableInstancesByName( String variableId, boolean onlyActiveProcesses ) {
        FindVariableInstancesByNameCommand cmd = new FindVariableInstancesByNameCommand();
        cmd.setVariableId(variableId);
        cmd.setActiveProcesses(onlyActiveProcesses);
        return (List<VariableInstanceLog>) executeCommand(cmd);
    }

    @Override
    public List<? extends VariableInstanceLog> findVariableInstancesByNameAndValue( String variableId, String value,
            boolean onlyActiveProcesses ) {
        FindVariableInstancesByNameCommand cmd = new FindVariableInstancesByNameCommand();
        cmd.setVariableId(variableId);
        cmd.setValue(value);
        cmd.setActiveProcesses(onlyActiveProcesses);
        return (List<VariableInstanceLog>) executeCommand(cmd);
    }

    @Override
    public void clear() {
        executeCommand(new ClearHistoryLogsCommand());
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Dispose does not need to be called on the Remote Client  " + AuditService.class.getSimpleName() + " implementation.");
    }

}
