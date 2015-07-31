/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.api.model.definition;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-task-inputs")
public class TaskInputsDefinition {

    @XmlElementWrapper(name="inputs")
    private Map<String, String> taskInputs;

    public TaskInputsDefinition() {
        this(new HashMap<String, String>());
    }

    public TaskInputsDefinition(Map<String, String> taskInputs) {
        this.taskInputs = taskInputs;
    }

    public Map<String, String> getTaskInputs() {
        return taskInputs;
    }

    public void setTaskInputs(Map<String, String> taskInputs) {
        this.taskInputs = taskInputs;
    }

    @Override
    public String toString() {
        return "TaskInputsDefinition{" +
                "taskInputs=" + taskInputs +
                '}';
    }
}
