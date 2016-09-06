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

package org.kie.remote.services.util;

import java.util.Map;

import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.TaskContentRegistry;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Task;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.ContentMarshallerContext;

public class ExecuteAndSerializeCommand<T> extends TaskCommand<T>{

    private TaskCommand<T> command;
    private String deploymentId;

    public ExecuteAndSerializeCommand() {

    }

    public ExecuteAndSerializeCommand(TaskCommand<T> command, String deploymentId) {
        this.command = command;
        this.deploymentId = deploymentId;
    }

    @Override
    public T execute(Context context) {
        T cmdResult =  command.execute(context);
        if( cmdResult == null ) {
            return null;
        }
        if( cmdResult instanceof Task) {
            cmdResult = (T) new JaxbTask((Task) cmdResult);
        } else if( cmdResult instanceof Content) {
            Content content = (Content) cmdResult;

            JaxbContent jaxbContent = new JaxbContent();
            jaxbContent.setId(content.getId());
            jaxbContent.setSerializedContent(content.getContent());

            ContentMarshallerContext contentMarshallerContext = TaskContentRegistry.get().getMarshallerContext(deploymentId);
            Object unmarshalledContent = null;
            if (contentMarshallerContext != null) {
                unmarshalledContent = ContentMarshallerHelper.unmarshall(content.getContent(), contentMarshallerContext.getEnvironment(), contentMarshallerContext.getClassloader());
            } else {
                unmarshalledContent = ContentMarshallerHelper.unmarshall(content.getContent(), null);
            }
            if( unmarshalledContent != null && unmarshalledContent instanceof Map ) {
                jaxbContent.setContentMap((Map<String, Object>) unmarshalledContent);
            }
            cmdResult = (T) jaxbContent;
        } else if (cmdResult instanceof Map) {
            Map output = (Map) cmdResult;
            cmdResult = (T) new JaxbContent();
            ((JaxbContent) cmdResult).setContentMap(output);
            ((JaxbContent) cmdResult).setId(-1L);
        }
        return cmdResult;
    }
}
