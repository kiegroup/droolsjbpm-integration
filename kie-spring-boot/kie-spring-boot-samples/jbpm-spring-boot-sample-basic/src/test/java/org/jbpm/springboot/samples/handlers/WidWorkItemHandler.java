/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples.handlers;

import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.jbpm.process.workitem.core.util.service.WidAction;
import org.jbpm.process.workitem.core.util.service.WidService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

@Wid(widfile = "WidCustomDefinitions.wid", name = "WidCustom",
    displayName = "WidCustom",
    defaultHandler = "mvel: new org.jbpm.springboot.samples.handlers.WidWorkItemHandler()",
    documentation = "${artifactId}/index.html",
    parameters = {
        @WidParameter(name = "Param")
    },
    results = {
        @WidResult(name = "Result")
    },
    mavenDepends = {
        @WidMavenDepends(group = "${groupId}", artifact = "${artifactId}", version = "${version}")
    },
    serviceInfo = @WidService(category = "${name}", description = "${description}",
        keywords = "custom wid annotated handler",
        action = @WidAction(title = "Custom execution of business logic")
))
public class WidWorkItemHandler implements WorkItemHandler {

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        
    }

}
