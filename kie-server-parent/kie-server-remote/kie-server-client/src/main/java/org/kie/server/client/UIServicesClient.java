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

package org.kie.server.client;

import org.kie.server.client.jms.ResponseHandler;

public interface UIServicesClient {

    public static final String FORM_MODELLER_TYPE = "FORM";
    public static final String FORM_TYPE = "FRM";
    public static final String FREE_MARKER_TYPE = "FTL";
    public static final String ANY_FORM = "ANY";
    
    public static final String BOOTSTRAP_FORM_RENDERER = "bootstrap";
    public static final String PATTERNFLY_FORM_RENDERER = "patternfly";
    public static final String WORKBENCH_FORM_RENDERER = "workbench";

    /**
     * Returns process form for given process id that resides in given container. It returns default form type
     * which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @param language language that form should be filtered for
     * @return string representation (json or xml depending on client marshaling selection) of the process form
     */
    String getProcessForm(String containerId, String processId, String language);

    /**
     * Returns process form for given process id that resides in given container
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @param language language that form should be filtered for
     * @param formType type of form to be returned (FORM - default (form modeler), FRM - v7 forms, FTL - freemarker template)
     * @return string representation (json or xml depending on client marshaling selection) of the process form
     */
    String getProcessFormByType(String containerId, String processId, String language, String formType);

    /**
     * Returns process form for given process id that resides in given container - without filtering values by language. It returns default form type
     * which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @return string representation (json or xml depending on client marshaling selection) of the process form
     */
    String getProcessForm(String containerId, String processId);

    /**
     * Returns process form for given process id that resides in given container - without filtering values by language. It returns default form type
     * which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @return string representation of the process form without any marshalling
     */
    String getProcessRawForm(String containerId, String processId);

    /**
     * Returns process form for given process id that resides in given container - without filtering values by language
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @param formType type of form to be returned (FORM - default (form modeler), FRM - v7 forms, FTL - freemarker template)
     * @return string representation (json or xml depending on client marshaling selection) of the process form
     */
    String getProcessFormByType(String containerId, String processId, String formType);

    /**
     * Returns task form for given task id that belongs to given container. It returns default form type
     * which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param language language that form should be filtered for
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskForm(String containerId, Long taskId, String language);

    /**
     * Returns task form for given task id that belongs to given container.
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param language language that form should be filtered for
     * @param formType type of form to be returned (FORM - default (form modeler), FRM - v7 forms, FTL - freemarker template)
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskFormByType(String containerId, Long taskId, String language, String formType);

    /**
     * Returns task form for given task id that belongs to given container as raw content - without filtering values by language.
     * It returns default form type which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskForm(String containerId, Long taskId);

    /**
     * Returns task form for given task id that belongs to given container as raw content - without filtering values by language.
     * It returns default form type which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @return  string representation of the task form without any marshalling
     */
    String getTaskRawForm(String containerId, Long taskId);

    /**
     * Returns task form for given task id that belongs to given container as raw content - without filtering values by language.
     * It returns default form type which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param formType type of form to be returned (FORM - default (form modeler), FRM - v7 forms, FTL - freemarker template)
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskFormByType(String containerId, Long taskId, String formType);

    /**
     * Returns task form for given task id that belongs to given container as specified user. It returns default form type
     * which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormForUserByType
     * Introduced to allow client to take advantage of bypass auth user connecting to server.
     * @param userId userId making the request to bypass auth user
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param language language that form should be filtered for
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskFormAsUser(String userId, String containerId, Long taskId, String language);

    /**
     * Returns task form for given task id that belongs to given container as specified user.
     * Introduced to allow client to take advantage of bypass auth user connecting to server.
     * @param userId userId making the request to bypass auth user
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param language language that form should be filtered for
     * @param formType type of form to be returned (FORM - default (form modeler), FRM - v7 forms, FTL - freemarker template)
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskFormByTypeAsUser(String userId, String containerId, Long taskId, String language, String formType);

    /**
     * Returns task form for given task id that belongs to given container as specified user as raw content - without filtering values by language.
     * It returns default form type which is (FORM - build with form modeler). If there is a need to select the type use #getProcessFormByType
     * Introduced to allow client to take advantage of bypass auth user connecting to server.
     * @param userId userId making the request to bypass auth user
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @return  string representation of the task form without any marshalling
     */
    String getTaskRawFormAsUser(String userId, String containerId, Long taskId);

    /**
     * Returns process image (svg) of the given process id that belongs to given container
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @return svg (xml) representing process image
     */
    String getProcessImage(String containerId, String processId);

    /**
     * Returns process image (svg) with annotated active and completed nodes for given process instance
     * that belongs to given container
     * @param containerId container identifier where process resides
     * @param processInstanceId unique process instance id
     * @return svg (xml) representing process image annotated with active (in red) and completed (in grey) nodes
     */
    String getProcessInstanceImage(String containerId, Long processInstanceId);

    /**
     * Returns process image (svg) with annotated active and completed nodes for given process instance
     * that belongs to given container
     * @param containerId container identifier where process resides
     * @param processInstanceId unique process instance id
     * @param completeNodeColor process instance image complete node color
     * @param completeNodeBorderColor process instance image complete node border color
     * @param activeNodeBorderColor process instance image active node border color
     * @return svg (xml) representing process image annotated with custom colors
     */
    String getProcessInstanceImageCustomColor(String containerId, Long processInstanceId, String completeNodeColor,
                                              String completeNodeBorderColor, String activeNodeBorderColor);

    /**
     * Returns process form for given process id that resides in given container - completely rendered so the output is HTML
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @return HTML representation of the process form
     */
    String renderProcessForm(String containerId, String processId);
    
    /**
     * Returns process form for given process id that resides in given container - completely rendered so the output is HTML
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @param renderer name of the renderer to be used to produce the HTML
     * @return HTML representation of the process form
     */
    String renderProcessForm(String containerId, String processId, String renderer);
    
    /**
     * Returns case form for given case definition that resides in given container - completely rendered so the output is HTML
     * @param containerId container identifier where process resides
     * @param caseDefinitionId  unique case definition id
     * @return HTML representation of the process form
     */
    String renderCaseForm(String containerId, String caseDefinitionId);
    
    /**
     * Returns case form for given case definition id that resides in given container - completely rendered so the output is HTML
     * @param containerId container identifier where process resides
     * @param caseDefinitionId  unique case definition id
     * @param renderer name of the renderer to be used to produce the HTML
     * @return HTML representation of the process form
     */
    String renderCaseForm(String containerId, String caseDefinitionId, String renderer);
    
    /**
     * Returns task form for given task id that belongs to given container - completely rendered so the output is HTML
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @return HTML representation of the task form
     */
    String renderTaskForm(String containerId, Long taskId);
    
    /**
     * Returns task form for given task id that belongs to given container - completely rendered so the output is HTML
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param renderer name of the renderer to be used to produce the HTML
     * @return HTML representation of the task form
     */
    String renderTaskForm(String containerId, Long taskId, String renderer);

    /**
     * Override default response handler to change interaction pattern. Applies only to JMS
     * based integration.
     * @param responseHandler
     */
    void setResponseHandler(ResponseHandler responseHandler);
}
