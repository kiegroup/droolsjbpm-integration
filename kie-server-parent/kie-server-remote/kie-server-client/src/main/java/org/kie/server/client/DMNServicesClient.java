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

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ServiceResponse;

public interface DMNServicesClient {

    /**
     * Evaluate all decisions for the model identified by namespace and modelName, given the context dmnContext
     *
     * @param containerId the container id deploying the DMN model
     * @param namespace namespace to identify the model to evaluate
     * @param modelName model name to identify the model to evaluate
     * @param dmnContext the context with all the input variables
     *
     * @return the result of the evaluation
     */
    ServiceResponse<DMNResult> evaluateAll(String containerId, String namespace, String modelName, DMNContext dmnContext);
    
    /**
     * Evaluate the decision identified by the given name and all dependent decisions for the model identified by namespace and modelName, given the context dmnContext
     *
     * @param containerId the container id deploying the DMN model
     * @param namespace namespace to identify the model to evaluate
     * @param modelName model name to identify the model to evaluate
     * @param decisionName the root decision to evaluate, identified
     *                     by name
     * @param dmnContext the context with all the input variables
     *
     * @return the result of the evaluation
     */
    ServiceResponse<DMNResult> evaluateDecisionByName(String containerId, String namespace, String modelName, String decisionName, DMNContext dmnContext);
    
    /**
     * Evaluate the decision identified by the given ID and all dependent decisions for the model identified by namespace and modelName, given the context dmnContext
     *
     * @param containerId the container id deploying the DMN model
     * @param namespace namespace to identify the model to evaluate
     * @param modelName model name to identify the model to evaluate
     * @param decisionId the root decision to evaluate, identified
     *                   by ID
     * @param dmnContext the context with all the input variables
     *
     * @return the result of the evaluation
     */
    ServiceResponse<DMNResult> evaluateDecisionById(String containerId, String namespace, String modelName, String decisionId, DMNContext dmnContext);
    
    /**
     * Creates a new empty DMNContext
     *
     * @return a new empty DMNContext
     */
    DMNContext newContext();

    /**
     * Convenience method to be used if the container contains only a single DMN model, to evaluate all decisions.
     * The method {@link DMNServicesClient#evaluateAllDecisions(String, String, String, DMNContext)} shall be used for containers deploying multiple DMN model.
     *
     * @param containerId the container id deploying the DMN model
     * @param dmnContext the context with all the input variables
     *
     * @return the result of the evaluation, or an error if the container contains more than a single DMN model
     */
    ServiceResponse<DMNResult> evaluateAll(String containerId, DMNContext dmnContext);

}
