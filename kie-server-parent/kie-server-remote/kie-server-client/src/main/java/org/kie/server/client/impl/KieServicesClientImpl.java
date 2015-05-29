package org.kie.server.client.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.api.command.Command;
import org.kie.internal.process.CorrelationKey;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.RegisterServerControllerCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskAttachmentList;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskCommentList;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskEventInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;

public class KieServicesClientImpl
        implements KieServicesClient {
    private static Logger logger = LoggerFactory.getLogger( KieServicesClientImpl.class );

    private static final Boolean BYPASS_AUTH_USER = Boolean.parseBoolean(System.getProperty("org.kie.server.bypass.auth.user", "false"));

    private       String                   baseURI;
    private final KieServicesConfiguration config;
    private final Marshaller               marshaller;

    public KieServicesClientImpl(KieServicesConfiguration config) {
        this.config = config.clone();
        this.baseURI = config.getServerUrl();
        ClassLoader cl = Thread.currentThread().getContextClassLoader() != null ? Thread.currentThread().getContextClassLoader() : CommandScript.class.getClassLoader();
        this.marshaller = MarshallerFactory.getMarshaller( config.getExtraJaxbClasses(), config.getMarshallingFormat(), cl );
    }

    public KieServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        this.config = config.clone();
        this.baseURI = config.getServerUrl();
        this.marshaller = MarshallerFactory.getMarshaller( config.getExtraJaxbClasses(), config.getMarshallingFormat(), classLoader );
    }

    /**
     * Initializes the URL that will be used for web service access
     *
     * @param url URL of the server instance
     * @return An URL that can be used for the web services
     */
    private String initializeURI(URL url, String servicePrefix) {
        if ( url == null ) {
            throw new IllegalArgumentException( "The url may not be empty or null." );
        }
        try {
            url.toURI();
        } catch ( URISyntaxException urise ) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + urise.getMessage(), urise );
        }

        String urlString = url.toExternalForm();
        if ( !urlString.endsWith( "/" ) ) {
            urlString += "/";
        }
        urlString += "services/"+servicePrefix + "/server";

        URL serverPlusServicePrefixUrl;
        try {
            serverPlusServicePrefixUrl = new URL( urlString );
        } catch ( MalformedURLException murle ) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + murle.getMessage(), murle );
        }

        return urlString;
    }

    @Override
    public ServiceResponse<KieServerInfo> register(String controllerEndpoint, KieServerConfig kieServerConfig) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( baseURI+"/controller/"+controllerEndpoint, kieServerConfig, KieServerInfo.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new RegisterServerControllerCommand( controllerEndpoint, kieServerConfig ) ) );
            return (ServiceResponse<KieServerInfo>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieServerInfo> getServerInfo() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI, KieServerInfo.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetServerInfoCommand() ) );
            return (ServiceResponse<KieServerInfo>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResourceList> listContainers() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers", KieContainerResourceList.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new ListContainersCommand() ) );
            return (ServiceResponse<KieContainerResourceList>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        if( config.isRest() ) {
            return makeHttpPutRequestAndCreateServiceResponse( baseURI + "/containers/" + id, resource, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CreateContainerCommand( resource ) ) );
            return (ServiceResponse<KieContainerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetContainerInfoCommand( id ) ) );
            return (ServiceResponse<KieContainerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<Void> disposeContainer(String id) {
        if( config.isRest() ) {
            return makeHttpDeleteRequestAndCreateServiceResponse( baseURI + "/containers/" + id, Void.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DisposeContainerCommand( id ) ) );
            return (ServiceResponse<Void>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<String> executeCommands(String id, String payload) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( baseURI + "/containers/" + id, payload, String.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, payload ) ) );
            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<String> executeCommands(String id, Command<?> cmd) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( baseURI + "/containers/" + id, cmd, String.class, getHeaders(cmd) );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, serialize(cmd) ) ) );
            return (ServiceResponse<String>) executeJmsCommand( script, cmd.getClass().getName() ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript script) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateCustomResponse( baseURI, script, ServiceResponsesList.class );
        } else {
            return executeJmsCommand( script );
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id + "/scanner", KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetScannerInfoCommand( id ) ) );
            return (ServiceResponse<KieScannerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    baseURI + "/containers/" + id + "/scanner", resource,
                    KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateScannerCommand( id, resource ) ) );
            return (ServiceResponse<KieScannerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    baseURI + "/containers/" + id + "/release-id", releaseId,
                    ReleaseId.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateReleaseIdCommand( id, releaseId ) ) );
            return (ServiceResponse<ReleaseId>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    /*
     * jBPM part START
     * Process definition related operations
     */

    @Override
    public ProcessDefinition getProcessDefinition(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_GET_URI, valuesMap),
                    ProcessDefinition.class);
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public SubProcessesDefinition getReusableSubProcessDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_SUBPROCESS_GET_URI, valuesMap),
                    SubProcessesDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public VariablesDefinition getProcessVariableDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_VARIABLES_GET_URI, valuesMap),
                    VariablesDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ServiceTasksDefinition getServiceTaskDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_SERVICE_TASKS_GET_URI, valuesMap),
                    ServiceTasksDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public AssociatedEntitiesDefinition getAssociatedEntityDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_ASSOCIATED_ENTITIES_GET_URI, valuesMap),
                    AssociatedEntitiesDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public UserTaskDefinitionList getUserTaskDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_USER_TASKS_GET_URI, valuesMap),
                    UserTaskDefinitionList.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInputsDefinition getUserTaskInputDefinitions(String containerId, String processId, String taskName) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);
            valuesMap.put(TASK_NAME, taskName);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_USER_TASK_INPUT_GET_URI, valuesMap),
                    TaskInputsDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskOutputsDefinition getUserTaskOutputDefinitions(String containerId, String processId, String taskName) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);
            valuesMap.put(TASK_NAME, taskName);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEF_USER_TASK_OUTPUT_GET_URI, valuesMap),
                    TaskOutputsDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long startProcess(String containerId, String processId) {
        return startProcess(containerId, processId, (Map<String, Object>) null);
    }

    @Override
    public Long startProcess(String containerId, String processId, Map<String, Object> variables) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            Object result = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, START_PROCESS_POST_URI, valuesMap), variables,
                    Object.class);

            if (result instanceof Wrapped) {
                return (Long) ((Wrapped) result).unwrap();
            }

            return ((Number) result).longValue();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long startProcess(String containerId, String processId, CorrelationKey correlationKey) {
        return startProcess(containerId, processId, correlationKey, null);
    }

    @Override
    public Long startProcess(String containerId, String processId, CorrelationKey correlationKey, Map<String, Object> variables) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            Object result = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, START_PROCESS_WITH_CORRELATION_KEY_POST_URI, valuesMap), variables,
                    Object.class);

            if (result instanceof Wrapped) {
                return (Long) ((Wrapped) result).unwrap();
            }

            return ((Number) result).longValue();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void abortProcessInstance(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, ABORT_PROCESS_INST_DEL_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void abortProcessInstances(String containerId, List<Long> processInstanceIds) {
        if( config.isRest() ) {
            String queryStr = buildQueryString("instanceId", processInstanceIds);

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, ABORT_PROCESS_INSTANCES_DEL_URI, valuesMap) + queryStr,
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Object getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName) {
        return getProcessInstanceVariable(containerId, processInstanceId, variableName, Object.class);
    }

    @Override
    public <T> T getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName, Class<T> type) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableName);

            Object result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_VAR_GET_URI, valuesMap), type);

            if (result instanceof Wrapped) {
                return (T) ((Wrapped) result).unwrap();
            }

            return (T) result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Map<String, Object> getProcessInstanceVariables(String containerId, Long processInstanceId) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            Object variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_VARS_GET_URI, valuesMap),
                    Object.class);

            if (variables instanceof Wrapped) {
                return (Map) ((Wrapped) variables).unwrap();
            }

            return (Map) variables;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void signalProcessInstance(String containerId, Long processInstanceId, String signalName, Object event) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(SIGNAL_NAME, signalName);

            Map<String, String> headers = new HashMap<String, String>();

            makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, SIGNAL_PROCESS_INST_POST_URI, valuesMap), event, String.class, headers);
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void signalProcessInstances(String containerId, List<Long> processInstanceIds, String signalName, Object event) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(SIGNAL_NAME, signalName);

            String queryStr = buildQueryString("instanceId", processInstanceIds);


            Map<String, String> headers = new HashMap<String, String>();
            makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, SIGNAL_PROCESS_INSTANCES_PORT_URI, valuesMap) + queryStr
                    , event, String.class, headers);
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<String> getAvailableSignals(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            Object signals = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_SIGNALS_GET_URI, valuesMap), Object.class);

            if (signals instanceof Wrapped) {
                return (List<String>) ((Wrapped)signals).unwrap();
            }

            return (List<String>) signals;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setProcessVariable(String containerId, Long processInstanceId, String variableId, Object value) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableId);
            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_VAR_PUT_URI, valuesMap), value, String.class, getHeaders(null));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setProcessVariables(String containerId, Long processInstanceId, Map<String, Object> variables) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_VARS_POST_URI, valuesMap), variables,
                    String.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessInstance getProcessInstance(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_GET_URI, valuesMap) , ProcessInstance.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessInstance getProcessInstance(String containerId, Long processInstanceId, boolean withVars) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_GET_URI, valuesMap) + "?withVars=" + withVars , ProcessInstance.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void completeWorkItem(String containerId, Long processInstanceId, Long id, Map<String, Object> results) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, id);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_WORK_ITEM_COMPLETE_PUT_URI, valuesMap), results,
                    String.class, getHeaders(null));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void abortWorkItem(String containerId, Long processInstanceId, Long id) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, id);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI, valuesMap), null,
                    String.class, getHeaders(null));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public WorkItemInstance getWorkItem(String containerId, Long processInstanceId, Long id) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, id);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_WORK_ITEM_BY_ID_GET_URI, valuesMap), WorkItemInstance.class);


        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<WorkItemInstance> getWorkItemByProcessInstance(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            WorkItemInstanceList list = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_WORK_ITEMS_BY_PROC_INST_ID_GET_URI, valuesMap), WorkItemInstanceList.class);

            if (list != null && list.getWorkItems() != null) {
                return Arrays.asList(list.getWorkItems());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void activateTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_ACTIVATE_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void claimTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_CLAIM_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void completeTask(String containerId, Long taskId, String userId, Map<String, Object> params) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMPLETE_PUT_URI, valuesMap) + getUserQueryStr(userId),
                    params, String.class, getHeaders(null));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void delegateTask(String containerId, Long taskId, String userId, String targetUserId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_DELEGATE_PUT_URI, getUserAndAdditionalParam(userId, "targetUser", targetUserId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void exitTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_EXIT_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void failTask(String containerId, Long taskId, String userId, Map<String, Object> params) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_FAIL_PUT_URI, valuesMap) + getUserQueryStr(userId),
                    params, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void forwardTask(String containerId, Long taskId, String userId, String targetEntityId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_FORWARD_PUT_URI, getUserAndAdditionalParam(userId, "targetUser", targetEntityId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void releaseTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_RELEASE_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void resumeTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_RESUME_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void skipTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_SKIP_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void startTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_START_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void stopTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_STOP_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void suspendTask(String containerId, Long taskId, String userId) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_SUSPEND_PUT_URI, getUserQueryStr(userId));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void nominateTask(String containerId, Long taskId, String userId, List<String> potentialOwners) {
        if( config.isRest() ) {

            sendTaskOperation(containerId, taskId, TASK_INSTANCE_NOMINATE_PUT_URI, getUserAndAdditionalParams(userId, "potOwner", potentialOwners));
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskPriority(String containerId, Long taskId, int priority) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_PRIORITY_PUT_URI, valuesMap),
                    priority, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskExpirationDate(String containerId, Long taskId, Date date) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_EXPIRATION_DATE_PUT_URI, valuesMap),
                    date, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskSkipable(String containerId, Long taskId, boolean skipable) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_SKIPABLE_PUT_URI, valuesMap),
                    skipable, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskName(String containerId, Long taskId, String name) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_NAME_PUT_URI, valuesMap),
                    name, String.class, getHeaders(null));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void setTaskDescription(String containerId, Long taskId, String description) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_DESCRIPTION_PUT_URI, valuesMap),
                    description, String.class, getHeaders(description));

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long saveTaskContent(String containerId, Long taskId, Map<String, Object> values) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object contentId = makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_OUTPUT_DATA_PUT_URI, valuesMap),
                    values, Object.class, getHeaders(null));

            if (contentId instanceof Wrapped) {
                return (Long) ((Wrapped) contentId).unwrap();
            }

            return ((Number) contentId).longValue();
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Map<String, Object> getTaskOutputContentByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_OUTPUT_DATA_GET_URI, valuesMap), Object.class);

            if (variables instanceof Wrapped) {
                return (Map) ((Wrapped) variables).unwrap();
            }

            return (Map) variables;
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Map<String, Object> getTaskInputContentByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object variables = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_INPUT_DATA_GET_URI, valuesMap), Object.class);

            if (variables instanceof Wrapped) {
                return (Map) ((Wrapped) variables).unwrap();
            }

            return (Map) variables;
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void deleteTaskContent(String containerId, Long taskId, Long contentId) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(CONTENT_ID, contentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_CONTENT_DATA_DELETE_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long addTaskComment(String containerId, Long taskId, String text, String addedBy, Date addedOn) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskComment taskComment = TaskComment.builder()
                    .text(text)
                    .addedBy(addedBy)
                    .addedAt(addedOn)
                    .build();

            Object commentId = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENT_ADD_POST_URI, valuesMap), taskComment, Object.class, getHeaders(taskComment));

            if (commentId instanceof Wrapped) {
                return (Long) ((Wrapped) commentId).unwrap();
            }

            return ((Number) commentId).longValue();
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void deleteTaskComment(String containerId, Long taskId, Long commentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(COMMENT_ID, commentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENT_DELETE_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskComment> getTaskCommentsByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskCommentList commentList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENTS_GET_URI, valuesMap), TaskCommentList.class);

            if (commentList.getTasks() != null) {
                return Arrays.asList(commentList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskComment getTaskCommentById(String containerId, Long taskId, Long commentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(COMMENT_ID, commentId);

            TaskComment taskComment = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_COMMENT_GET_URI, valuesMap), TaskComment.class);

            return taskComment;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long addTaskAttachment(String containerId, Long taskId, String userId, Object attachment) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            Object attachmentId = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_ADD_POST_URI, valuesMap) + getUserQueryStr(userId),
                    attachment, Object.class, getHeaders(null));

            if (attachmentId instanceof Wrapped) {
                return (Long) ((Wrapped) attachmentId).unwrap();
            }

            return ((Number) attachmentId).longValue();
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void deleteTaskAttachment(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_DELETE_URI, valuesMap),
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskAttachment getTaskAttachmentById(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            TaskAttachment attachment = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_GET_URI, valuesMap), TaskAttachment.class);

            return attachment;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Object getTaskAttachmentContentById(String containerId, Long taskId, Long attachmentId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);
            valuesMap.put(ATTACHMENT_ID, attachmentId);

            Object result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENT_CONTENT_GET_URI, valuesMap), Object.class);

            if (result instanceof Wrapped) {
                return ((Wrapped) result).unwrap();
            }

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskAttachment> getTaskAttachmentsByTaskId(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskAttachmentList attachmentList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_ATTACHMENTS_GET_URI, valuesMap), TaskAttachmentList.class);

            if (attachmentList.getTasks() != null) {
                return Arrays.asList(attachmentList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance getTaskInstance(String containerId, Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            TaskInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_GET_URI, valuesMap), TaskInstance.class);


            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance getTaskInstance(String containerId, Long taskId, boolean withInputs, boolean withOutputs, boolean withAssignments) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            StringBuilder queryString = new StringBuilder();
            queryString.append("?withInputData").append("=").append(withInputs)
                    .append("&withOutputData").append("=").append(withOutputs)
                    .append("&withAssignments").append("=").append(withAssignments);

            TaskInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_INSTANCE_GET_URI, valuesMap) + queryString.toString(), TaskInstance.class);


            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcessesById(String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_BY_ID_GET_URI, valuesMap), ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessDefinition findProcessByContainerIdProcessId(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            ProcessDefinition result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI, valuesMap), ProcessDefinition.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcesses(Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?filter=" + filter, page, pageSize);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = getPagingQueryString("", page, pageSize);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?processName=" + processName, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?initiator=" + initiator, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessInstance findProcessInstanceById(Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            ProcessInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI, valuesMap), ProcessInstance.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessInstance findProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            ProcessInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI, valuesMap), ProcessInstance.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public NodeInstance findNodeInstanceByWorkItemId(Long processInstanceId, Long workItemId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, workItemId);

            NodeInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI, valuesMap), NodeInstance.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<NodeInstance> findActiveNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("?activeOnly=true", page, pageSize);

            NodeInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

            if (result != null && result.getNodeInstances() != null) {
                return Arrays.asList(result.getNodeInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<NodeInstance> findCompletedNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("?completedOnly=true", page, pageSize);

            NodeInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

            if (result != null && result.getNodeInstances() != null) {
                return Arrays.asList(result.getNodeInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<NodeInstance> findNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("", page, pageSize);

            NodeInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

            if (result != null && result.getNodeInstances() != null) {
                return Arrays.asList(result.getNodeInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<VariableInstance> findVariablesCurrentState(Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            VariableInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, VAR_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap), VariableInstanceList.class);

            if (result != null && result.getVariableInstances() != null) {
                return Arrays.asList(result.getVariableInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<VariableInstance> findVariableHistory(Long processInstanceId, String variableName, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableName);

            String queryString = getPagingQueryString("", page, pageSize);

            VariableInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI, valuesMap) + queryString, VariableInstanceList.class);

            if (result != null && result.getVariableInstances() != null) {
                return Arrays.asList(result.getVariableInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance findTaskByWorkItemId(Long workItemId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(WORK_ITEM_ID, workItemId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_BY_WORK_ITEM_ID_GET_URI, valuesMap), TaskInstance.class);


        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInstance findTaskById(Long taskId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_GET_URI, valuesMap), TaskInstance.class);


        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsBusinessAdministrator(String userId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_BUSINESS_ADMINS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();


            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksAssignedAsPotentialOwner(String userId, List<String> groups, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String groupsQuery = getAdditionalParams(statusQuery, "groups", groups);
            String queryString = getPagingQueryString(groupsQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_ASSIGN_POT_OWNERS_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_OWNED_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksOwned(String userId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String userQuery = getUserQueryStr(userId);
            String statusQuery = getAdditionalParams(userQuery, "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_OWNED_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasksByStatusByProcessInstanceId(Long processInstanceId, List<String> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String statusQuery = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASK_BY_PROCESS_INST_ID_GET_URI, valuesMap) + queryString, TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskSummary> findTasks(String userId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();


            String queryString = getUserAndPagingQueryString(userId, page, pageSize);

            TaskSummaryList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_GET_URI, valuesMap) + queryString , TaskSummaryList.class);

            if (taskSummaryList != null && taskSummaryList.getTasks() != null) {
                return Arrays.asList(taskSummaryList.getTasks());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<TaskEventInstance> findTaskEvents(Long taskId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(TASK_INSTANCE_ID, taskId);

            String queryString = getPagingQueryString("", page, pageSize);

            TaskEventInstanceList taskSummaryList = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, TASKS_EVENTS_GET_URI, valuesMap) + queryString , TaskEventInstanceList.class);

            if (taskSummaryList != null && taskSummaryList.getTaskEvents() != null) {
                return Arrays.asList(taskSummaryList.getTaskEvents());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }


    /*
     * jBPM part END
     *
     */

    private void sendTaskOperation(String containerId, Long taskId, String operation, String queryString) {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(CONTAINER_ID, containerId);
        valuesMap.put(TASK_INSTANCE_ID, taskId);

        makeHttpPutRequestAndCreateCustomResponse(
                build(baseURI, operation, valuesMap) + queryString, null, String.class, getHeaders(null));
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        logger.debug("About to send GET request to '{}'", uri);
        KieRemoteHttpRequest request = newRequest( uri ).get();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> T makeHttpGetRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        logger.debug("About to send GET request to '{}'", uri);
        KieRemoteHttpRequest request = newRequest( uri ).get();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {

            return deserialize(response.body(), resultType);

        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPostRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType );
    }

    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType, Map<String, String> headers) {
        return makeHttpPostRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType, headers );
    }

    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        return  makeHttpPostRequestAndCreateServiceResponse( uri, body, resultType, new HashMap<String, String>() );
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieRemoteHttpRequest request = newRequest( uri ).headers(headers).body(body).post();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }


    private <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType, Map<String, String> headers) {
        return makeHttpPostRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType, headers);
    }

    private <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        return makeHttpPostRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType, new HashMap<String, String>() );
    }

    private <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieRemoteHttpRequest request = newRequest( uri ).headers(headers).body(body).post();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode()
                || response.code() == Response.Status.CREATED.getStatusCode()) {
            return deserialize( response.body(), resultType );
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPutRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType );
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieRemoteHttpRequest request = newRequest(uri).body(body).put();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
             response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> T makeHttpPutRequestAndCreateCustomResponse(
            String uri, Object bodyObject,
            Class<T> resultType, Map<String, String> headers) {
        return makeHttpPutRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType, headers);
    }

    @SuppressWarnings("unchecked")
    private <T> T makeHttpPutRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieRemoteHttpRequest request = newRequest( uri ).headers(headers).body(body).put();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType );

            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpDeleteRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieRemoteHttpRequest request = newRequest( uri ).delete();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T makeHttpDeleteRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieRemoteHttpRequest request = newRequest( uri ).delete();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ||
                response.code() == Response.Status.NO_CONTENT.getStatusCode()) {
            if (resultType == null) {
                return null;
            }

            return deserialize( response.body(), resultType );
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private KieRemoteHttpRequest newRequest(String uri) {
        KieRemoteHttpRequest httpRequest =
                KieRemoteHttpRequest.newRequest( uri ).followRedirects( true ).timeout( config.getTimeout() );
        httpRequest.accept( getMediaType( config.getMarshallingFormat() ) );
        httpRequest.header(KieServerConstants.KIE_CONTENT_TYPE_HEADER, config.getMarshallingFormat().toString());
        if ( config.getUserName() != null && config.getPassword() != null ) {
            httpRequest.basicAuthorization( config.getUserName(), config.getPassword() );
        }
        return httpRequest;
    }

    /**
     * Method to communicate with the backend via JMS.
     *
     * @param command The {@link org.kie.api.command.Command} object to be executed.
     * @return The result of the {@link org.kie.api.command.Command} object execution.
     */
    private ServiceResponsesList executeJmsCommand( CommandScript command ) {
        return executeJmsCommand(command, null);
    }

    private ServiceResponsesList executeJmsCommand( CommandScript command, String classType ) {
        ConnectionFactory factory = config.getConnectionFactory();
        Queue sendQueue = config.getRequestQueue();
        Queue responseQueue = config.getResponseQueue();

        Connection connection = null;
        Session session = null;
        ServiceResponsesList cmdResponse = null;
        String corrId = UUID.randomUUID().toString();
        String selector = "JMSCorrelationID = '" + corrId + "'";
        try {
            // setup
            MessageProducer producer;
            MessageConsumer consumer;
            try {
                if( config.getPassword() != null ) {
                    connection = factory.createConnection(config.getUserName(), config.getPassword());
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                producer = session.createProducer(sendQueue);
                consumer = session.createConsumer(responseQueue, selector);

                connection.start();
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to setup a JMS connection.", jmse);
            }

            // Create msg
            TextMessage textMsg;
            Marshaller marshaller;
            try {

                // serialize request
                marshaller = MarshallerFactory.getMarshaller( config.getExtraJaxbClasses(), config.getMarshallingFormat(), CommandScript.class.getClassLoader() );
                String xmlStr = marshaller.marshall( command );
                textMsg = session.createTextMessage(xmlStr);

                // set properties
                // 1. corr id
                textMsg.setJMSCorrelationID(corrId);
                // 2. serialization info
                textMsg.setIntProperty( JMSConstants.SERIALIZATION_FORMAT_PROPERTY_NAME, config.getMarshallingFormat().getId() );
                if (classType != null) {
                    textMsg.setStringProperty(JMSConstants.CLASS_TYPE_PROPERTY_NAME, classType);
                }
                // send
                producer.send(textMsg);
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to send a JMS message.", jmse);
            }

            // receive
            Message response;
            try {
                response = consumer.receive( config.getTimeout() );
            } catch( JMSException jmse ) {
                jmse.printStackTrace();
                throw new KieServicesException("Unable to receive or retrieve the JMS response.", jmse);
            }

            if( response == null ) {
                logger.warn("Response is empty");
                return null;
            }
            // extract response
            assert response != null: "Response is empty.";
            try {
                String responseStr = ((TextMessage) response).getText();
                cmdResponse = marshaller.unmarshall(responseStr, ServiceResponsesList.class);
                return cmdResponse;
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to extract " + ServiceResponsesList.class.getSimpleName()
                                                       + " instance from JMS response.", jmse);
            }
        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                    if( session != null ) {
                        session.close();
                    }
                } catch( JMSException jmse ) {
                    logger.warn("Unable to close connection or session!", jmse);
                }
            }
        }
    }


    private String getMediaType( MarshallingFormat format ) {
        switch ( format ) {
            case JAXB: return MediaType.APPLICATION_XML;
            case JSON: return MediaType.APPLICATION_JSON;
            default: return MediaType.APPLICATION_XML;
        }
    }

    private String serialize(Object object) {
        if (object == null) {
            return "";
        }

        try {
            return marshaller.marshall( object );
        } catch ( MarshallingException e ) {
            throw new KieServicesException( "Error while serializing request data!", e );
        }
    }

    private <T> T deserialize(String content, Class<T> type) {
        logger.debug("About to deserialize content: \n '{}' \n into type: '{}'", content, type);
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            return marshaller.unmarshall( content, type );
        } catch ( MarshallingException e ) {
            throw new KieServicesException( "Error while deserializing data received from server!", e );
        }
    }

    /**
     * Checks whether the specified {@code ServiceResponse} contains the expected result type. In case the type is different,
     * {@code KieServicesClientException} is thrown. This catches the errors early, before returning the result from the client.
     * Without this check users could experience {@code ClassCastException} when retrieving the result that does not have
     * the expected type.
     */
    private void checkResultType(ServiceResponse<?> serviceResponse, Class<?> expectedResultType) {
        Object actualResult = serviceResponse.getResult();
        if ( actualResult != null && !expectedResultType.isInstance( actualResult ) ) {
            throw new KieServicesException(
                    "Error while creating service response! The actual result type " +
                    serviceResponse.getResult().getClass() + " does not match the expected type " + expectedResultType + "!" );
        }
    }

    private RuntimeException createExceptionForUnexpectedResponseCode(
            KieRemoteHttpRequest request,
            KieRemoteHttpResponse response) {
        String summaryMessage = "Unexpected HTTP response code when requesting URI '" + request.getUri() + "'! Error code: " +
                                response.code() + ", message: " + response.message();
        logger.debug( summaryMessage + ", response body: " + response.body() );
        return new KieServicesException( summaryMessage );
    }

    private String buildQueryString(String paramName, List<?> items) {
        StringBuilder builder = new StringBuilder("?");
        for (Object o : items) {
            builder.append(paramName).append("=").append(o).append("&");
        }
        builder.deleteCharAt(builder.length()-1);

        return builder.toString();
    }

    private Map<String, String> getHeaders(Object object) {
        Map<String, String> headers = new HashMap<String, String>();
        if (object != null) {
            headers.put(KieServerConstants.CLASS_TYPE_HEADER, object.getClass().getName());
        }

        return headers;
    }

    private String getUserQueryStr(String userId) {
        if (BYPASS_AUTH_USER) {
            return "?user=" + userId;
        }

        return "";
    }

    private String getUserAndPagingQueryString(String userId, Integer page, Integer pageSize) {
        StringBuilder queryString = new StringBuilder(getUserQueryStr(userId));
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append("page=" + page).append("&pageSize=" + pageSize);

        return queryString.toString();
    }

    private String getUserAndAdditionalParam(String userId, String name, String value) {
        StringBuilder queryString = new StringBuilder(getUserQueryStr(userId));
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append(name).append("=").append(value);

        return queryString.toString();
    }

    private String getUserAndAdditionalParams(String userId, String name, List<?> values) {
        StringBuilder queryString = new StringBuilder(getUserQueryStr(userId));
        if (values != null) {
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            for (Object value : values) {
                queryString.append(name).append("=").append(value).append("&");
            }
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    private String getPagingQueryString(String inQueryString, Integer page, Integer pageSize) {
        StringBuilder queryString = new StringBuilder(inQueryString);
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append("page=" + page).append("&pageSize=" + pageSize);

        return queryString.toString();
    }

    private String getAdditionalParams(String inQueryString, String name, List<?> values) {
        StringBuilder queryString = new StringBuilder(inQueryString);

        if (values != null) {
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            for (Object value : values) {
                queryString.append(name).append("=").append(value).append("&");
            }
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

}
