/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning.data;

import java.util.Map;

import org.kie.server.api.model.taskassigning.TaskData;

import static org.kie.server.services.taskassigning.core.model.DefaultLabels.SKILLS;

public class DefaultTaskDataSkillsValueExtractor extends AbstractStringListValueAttributeMapValueExtractor<Map<String, Object>, TaskData> {

    static final String TASK_SKILLS_ATTRIBUTE = "skills";

    /**
     * Property for configuring the name of the task input parameter from where the skills will be extracted.
     */
    public static final String TASK_SKILLS_ATTRIBUTE_PROPERTY_NAME = DefaultTaskDataSkillsValueExtractor.class.getName() + "." + TASK_SKILLS_ATTRIBUTE;

    public DefaultTaskDataSkillsValueExtractor() {
        super(System.getProperty(TASK_SKILLS_ATTRIBUTE_PROPERTY_NAME, TASK_SKILLS_ATTRIBUTE),
              COMMA_SEPARATOR, TaskData.class, SKILLS.name(), 1);
    }

    @Override
    protected Map<String, Object> getAttributes(TaskData source) {
        return source.getInputData();
    }
}