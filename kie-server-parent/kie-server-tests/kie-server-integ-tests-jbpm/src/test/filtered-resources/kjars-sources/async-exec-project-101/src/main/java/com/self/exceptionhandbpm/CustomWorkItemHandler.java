/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package com.self.exceptionhandbpm;

import java.util.Map;
import java.util.HashMap;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class CustomWorkItemHandler implements WorkItemHandler {
    private Map<String, Object> results = new HashMap();

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        Object operation = workItem.getParameter("operation");
        System.out.println("CustomWorkItemHandler.executeWorkItem with operation: "
                            + operation + ", fixed");
        manager.completeWorkItem(workItem.getId(), this.results);
    }


    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }

    public CustomWorkItemHandler() {
    }
}