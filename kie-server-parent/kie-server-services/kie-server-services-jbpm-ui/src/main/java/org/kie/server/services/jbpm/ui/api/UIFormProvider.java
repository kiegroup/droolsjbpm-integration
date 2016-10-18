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

package org.kie.server.services.jbpm.ui.api;

import java.util.Map;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;

/**
 * Simple wrapper interface to allow use of FormProviders that can be discovered on runtime
 */
public interface UIFormProvider {

    /**
     * Invoked only once when first time loading the service so each provider will get the same
     * instance of formManagerService
     * @param formManagerService
     */
    void configure(FormManagerService formManagerService);

    /**
     * Rendering of process form
     * @param name
     * @param process
     * @param renderContext
     * @return
     */
    String render(String name, ProcessDefinition process, Map<String, Object> renderContext);

    /**
     * Rendering of task form
     * @param name
     * @param task
     * @param process
     * @param renderContext
     * @return
     */
    String render(String name, Task task, ProcessDefinition process, Map<String, Object> renderContext);

    /**
     * Return priority that defines how form providers are evaluated
     * @return
     */
    int getPriority();

    /**
     * Returns type of the form that is being returned - depends on form provider impl.
     * @return type of the content
     */
    String getType();
}
