package org.kie.server.client;

import java.util.List;
import java.util.Map;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.instance.ProcessInstance;

public interface KieServicesClient {
    ServiceResponse<KieServerInfo> register(String controllerEndpoint, KieServerConfig kieServerConfig);

    ServiceResponse<KieServerInfo> getServerInfo();

    ServiceResponse<KieContainerResourceList> listContainers();

    ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource);

    ServiceResponse<KieContainerResource> getContainerInfo(String id);

    ServiceResponse<Void> disposeContainer(String id);

    ServiceResponse<String> executeCommands(String id, String payload);

    ServiceResponsesList executeScript(CommandScript script);

    ServiceResponse<KieScannerResource> getScannerInfo(String id);

    ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource);

    ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId);

    // process definition
    ProcessDefinition getProcessDefinition(String containerId, String processId);

    SubProcessesDefinition getReusableSubProcessDefinitions(String containerId, String processId);

    VariablesDefinition getProcessVariableDefinitions(String containerId, String processId);

    ServiceTasksDefinition getServiceTaskDefinitions(String containerId, String processId);

    AssociatedEntitiesDefinition getAssociatedEntityDefinitions(String containerId, String processId);

    UserTaskDefinitionList getUserTaskDefinitions(String containerId, String processId);

    TaskInputsDefinition getUserTaskInputDefinitions(String containerId, String processId, String taskName);

    TaskOutputsDefinition getUserTaskOutputDefinitions(String containerId, String processId, String taskName);

    // process operations
    Long startProcess(String containerId, String processId);

    Long startProcess(String containerId, String processId, Map<String, Object> variables);

    void abortProcessInstance(String containerId, Long processInstanceId);

    void abortProcessInstances(String containerId, List<Long> processInstanceIds);

    Object getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName);

    <T> T getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName, Class<T> type);

    Map<String, Object> getProcessInstanceVariables(String containerId, Long processInstanceId);

    void signalProcessInstance(String containerId, Long processInstanceId, String signalName, Object event);

    void signalProcessInstances(String containerId, List<Long> processInstanceId, String signalName, Object event);

    List<String> getAvailableSignals(String containerId, Long processInstanceId);

    void setProcessVariable(String containerId, Long processInstanceId, String variableId, Object value);

    void setProcessVariables(String containerId, Long processInstanceId, Map<String, Object> variables);

    ProcessInstance getProcessInstance(String containerId, Long processInstanceId);
}
