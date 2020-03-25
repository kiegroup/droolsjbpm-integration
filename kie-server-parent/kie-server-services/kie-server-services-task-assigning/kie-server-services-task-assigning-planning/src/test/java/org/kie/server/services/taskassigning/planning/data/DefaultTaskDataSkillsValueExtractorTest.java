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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.core.model.DefaultLabels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.planning.data.DefaultTaskDataSkillsValueExtractor.TASK_SKILLS_ATTRIBUTE;

public class DefaultTaskDataSkillsValueExtractorTest extends AbstractStringListValueAttributeMapValueExtractorTest<Map<String, Object>, TaskData, DefaultTaskDataSkillsValueExtractor> {

    @Override
    protected DefaultTaskDataSkillsValueExtractor createExtractor() {
        return new DefaultTaskDataSkillsValueExtractor();
    }

    @Override
    protected Class<TaskData> getExpectedType() {
        return TaskData.class;
    }

    @Override
    protected int getExpectedPriority() {
        return 1;
    }

    @Override
    protected String getExpectedLabelName() {
        return DefaultLabels.SKILLS.name();
    }

    @Override
    protected String getExpectedAttributeName() {
        return TASK_SKILLS_ATTRIBUTE;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{mockTaskData(TASK_SKILLS_ATTRIBUTE, String.format("%s", VALUE1)), mockSet(VALUE1)});
        data.add(new Object[]{mockTaskData(TASK_SKILLS_ATTRIBUTE, String.format(" %s , ,%s", VALUE1, VALUE2)), mockSet(VALUE2, VALUE1)});
        data.add(new Object[]{mockTaskData(TASK_SKILLS_ATTRIBUTE, null), mockSet()});
        return data;
    }

    @Test
    public void getFromPropertyAttributeName() {
        System.setProperty(DefaultTaskDataSkillsValueExtractor.TASK_SKILLS_ATTRIBUTE_PROPERTY_NAME, CUSTOM_NAME);
        assertThat(createExtractor().getAttributeName()).isEqualTo(CUSTOM_NAME);
        System.clearProperty(DefaultTaskDataSkillsValueExtractor.TASK_SKILLS_ATTRIBUTE_PROPERTY_NAME);
    }
}