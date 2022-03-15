/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.UIServicesClient;

import static org.kie.server.api.rest.RestURI.*;

public class UIServicesClientImpl extends AbstractKieServicesClientImpl implements UIServicesClient {

    public UIServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public UIServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public String getProcessForm(String containerId, String processId, String language) {
        return getProcessFormByType( containerId, processId, language, ANY_FORM );
    }

    @Override
    public String getProcessFormByType(String containerId, String processId, String language, String formType) {
        return getProcessFormByType( containerId, processId, language, formType, true );
    }

    @Override
    public String getProcessRawForm( String containerId, String processId ) {
        return getProcessFormByType( containerId, processId, null, ANY_FORM, false );
    }

    @Override
    public String getProcessForm(String containerId, String processId) {
        return getProcessFormByType( containerId, processId, ANY_FORM );
    }

    @Override
    public String getProcessFormByType(String containerId, String processId, String formType) {
        return getProcessFormByType( containerId, processId, null, formType, true );
    }

    private String getProcessFormByType(String containerId, String processId, String language, String formType, boolean marshallContent ) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_ID, processId);

            StringBuffer params = new StringBuffer();

            params.append( "type=" ).append( formType );
            params.append( "&marshallContent=" ).append( marshallContent );
            boolean filter = false;
            if ( !StringUtils.isEmpty( language ) ) {
                params.append( "&lang=" ).append( language );
                filter = true;
            }
            params.append( "&filter=" ).append( filter );

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), FORM_URI + "/" + PROCESS_FORM_GET_URI, valuesMap) + "?" + params.toString() );

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "FormService", "getFormDisplayProcess", new Object[]{containerId, processId, StringUtils.defaultString( language ), !StringUtils.isEmpty( language ), formType } )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public String getTaskForm(String containerId, Long taskId, String language) {
        return getTaskFormByType(containerId, taskId, language, ANY_FORM);
    }

    @Override
    public String getTaskFormAsUser(String containerId, Long taskId, String language, String userId) {
        return getTaskFormByTypeAsUser(containerId, taskId, language, ANY_FORM, userId);
    }

    @Override
    public String getTaskFormByType(String containerId, Long taskId, String language, String formType) {
        return getTaskFormByTypeAsUser(containerId, taskId, language, formType, true, null);
    }

    @Override
    public String getTaskFormByTypeAsUser(String containerId, Long taskId, String language, String formType, String userId) {
        return getTaskFormByTypeAsUser(containerId, taskId, language, formType, true, userId);
    }

    private String getTaskFormByTypeAsUser(String containerId, Long taskId, String language, String formType, boolean marshallContent, String userId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.TASK_INSTANCE_ID, taskId);

            String userQuery = StringUtils.isEmpty(userId) ? "" : getUserQueryStr(userId);
            StringBuilder params = new StringBuilder(userQuery);
            if (params.length() == 0) {
                params.append("?");
            } else {
                params.append("&");
            }

            params.append( "type=" ).append( formType );
            params.append( "&marshallContent=" ).append( marshallContent );
            boolean filter = false;
            if ( !StringUtils.isEmpty( language ) ) {
                params.append( "&lang=" ).append( language );
                filter = true;
            }
            params.append( "&filter=" ).append( filter );

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), FORM_URI + "/" + TASK_FORM_GET_URI, valuesMap) + params.toString());

        } else {
            CommandScript script = new CommandScript(Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("FormService", "getFormDisplayTask",
                            new Object[]{containerId, taskId, StringUtils.defaultString(userId),
                                StringUtils.defaultString(language), !StringUtils.isEmpty(language), formType})));

            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand(script, DescriptorCommand.class.getName(), "BPM-UI", containerId).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public String getTaskForm(String containerId, Long taskId) {
        return getTaskFormByType(containerId, taskId, ANY_FORM);
    }

    @Override
    public String getTaskFormByType(String containerId, Long taskId, String formType) {
        return getTaskFormByTypeAsUser(containerId, taskId, null, formType, true, null);
    }

    @Override
    public String getTaskRawForm(String containerId, Long taskId) {
        return getTaskFormByTypeAsUser(containerId, taskId, null, ANY_FORM, false, null);
    }

    @Override
    public String getTaskRawFormAsUser(String containerId, Long taskId, String userId) {
        return getTaskFormByTypeAsUser(containerId, taskId, null, ANY_FORM, false, userId);
    }

    @Override
    public String getProcessImage(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_ID, processId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.APPLICATION_SVG_XML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), IMAGE_URI + "/" + PROCESS_IMG_GET_URI, valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ImageService", "getProcessImage", new Object[]{containerId, processId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public String getProcessInstanceImage(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_INST_ID, processInstanceId);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.APPLICATION_SVG_XML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), IMAGE_URI + "/" + PROCESS_INST_IMG_GET_URI, valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ImageService", "getActiveProcessImage", new Object[]{containerId, processInstanceId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    protected String createColorURLParams(String completeNodeColor, String completeNodeBorderColor, String activeNodeBorderColor, String activeAsyncNodeBorderColor) {
        List<String> params = new ArrayList<>();
        if(completeNodeColor != null && !completeNodeColor.isEmpty()) {
            params.add(RestURI.SVG_NODE_COMPLETED_COLOR + "=" + encode(completeNodeColor));
        }
        if(completeNodeBorderColor != null && !completeNodeBorderColor.isEmpty()) {
            params.add(RestURI.SVG_NODE_COMPLETED_BORDER_COLOR + "=" + encode(completeNodeBorderColor));
        }
        if(activeNodeBorderColor != null && !activeNodeBorderColor.isEmpty()) {
            params.add(RestURI.SVG_NODE_ACTIVE_COLOR + "=" + encode(activeNodeBorderColor));
        }

        if (activeAsyncNodeBorderColor != null && !activeAsyncNodeBorderColor.isEmpty()) {
            params.add(SVG_NODE_ACTIVE_ASYNC_BORDER_COLOR + "=" + encode(activeAsyncNodeBorderColor));
        }
        return String.join("&", params);
    }

    @Override
    public String getProcessInstanceImageCustomColor(String containerId, Long processInstanceId, String completeNodeColor,
                                                     String completeNodeBorderColor, String activeNodeBorderColor, String activeAsyncNodeBorderColor) {
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_INST_ID, processInstanceId);

            String colorURLParams = createColorURLParams(completeNodeColor, completeNodeBorderColor, activeNodeBorderColor, activeAsyncNodeBorderColor);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.APPLICATION_SVG_XML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), IMAGE_URI + "/" + PROCESS_INST_IMG_GET_URI + "?" + colorURLParams,
                          valuesMap), headers);
        } else {
            CommandScript script = new CommandScript(Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("ImageService",
                                                             "getActiveProcessImage",
                                                             new Object[]{
                                                                     containerId,
                                                                     processInstanceId,
                                                                     StringUtils.defaultString(completeNodeColor),
                                                                     StringUtils.defaultString(completeNodeBorderColor),
                                                                     StringUtils.defaultString(activeNodeBorderColor)})));
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand(script, DescriptorCommand.class.getName(), "BPM-UI", containerId).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public String renderProcessForm(String containerId, String processId) {
        return renderProcessForm(containerId, processId, PATTERNFLY_FORM_RENDERER);
    }

    @Override
    public String renderProcessForm(String containerId, String processId, String renderer) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.PROCESS_ID, processId);

            StringBuffer params = new StringBuffer();
            if (renderer != null) {
                params.append("?renderer=").append(renderer);
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.TEXT_HTML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), FORM_URI + "/" + PROCESS_FORM_CONTENT_GET_URI + params.toString(), valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "FormRendererService", "getProcessRenderedForm", new Object[]{renderer, containerId, processId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public String renderCaseForm(String containerId, String caseDefinitionId) {
        return renderCaseForm(containerId, caseDefinitionId, PATTERNFLY_FORM_RENDERER);
    }

    @Override
    public String renderCaseForm(String containerId, String caseDefinitionId, String renderer) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.CASE_DEF_ID, caseDefinitionId);

            StringBuffer params = new StringBuffer();
            if (renderer != null) {
                params.append("?renderer=").append(renderer);
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.TEXT_HTML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), FORM_URI + "/" + CASE_FORM_CONTENT_GET_URI + params.toString(), valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "FormRendererService", "getCaseRenderedForm", new Object[]{renderer, containerId, caseDefinitionId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }

    @Override
    public String renderTaskForm(String containerId, Long taskId) {
        return renderTaskForm(containerId, taskId, PATTERNFLY_FORM_RENDERER);
    }

    @Override
    public String renderTaskForm(String containerId, Long taskId, String renderer) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(RestURI.CONTAINER_ID, containerId);
            valuesMap.put(RestURI.TASK_INSTANCE_ID, taskId);

            StringBuffer params = new StringBuffer();
            if (renderer != null) {
                params.append("?renderer=").append(renderer);
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", MediaType.TEXT_HTML);

            return makeHttpGetRequestAndCreateRawResponse(
                    build(loadBalancer.getUrl(), FORM_URI + "/" + TASK_FORM_CONTENT_GET_URI + params.toString(), valuesMap), headers);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "FormRendererService", "getTaskRenderedForm", new Object[]{renderer, containerId, taskId} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM-UI", containerId ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            return response.getResult();
        }
    }
}
