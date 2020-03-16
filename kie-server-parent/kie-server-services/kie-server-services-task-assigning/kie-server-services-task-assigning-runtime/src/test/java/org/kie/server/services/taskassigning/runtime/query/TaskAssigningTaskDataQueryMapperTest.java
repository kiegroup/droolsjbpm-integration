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

package org.kie.server.services.taskassigning.runtime.query;

import java.util.List;

import org.kie.server.api.model.taskassigning.TaskData;

public class TaskAssigningTaskDataQueryMapperTest extends TaskAssigningTaskDataWithPotentialOwnersQueryMapperTest {

    @Override
    protected TaskAssigningTaskDataWithPotentialOwnersQueryMapper createQueryMapper() {
        return TaskAssigningTaskDataQueryMapper.get();
    }

    @Override
    protected String getExpectedName() {
        return TaskAssigningTaskDataQueryMapper.NAME;
    }

    @Override
    protected boolean readPotentialOwnersExpectedValue() {
        return false;
    }

    @Override
    protected void verifyResult(List<?> result) {
        List<TaskData> taskDataResult = (List<TaskData>) result;
        assertTask1IsPresent(taskDataResult, 0, false);
        assertTask2IsPresent(taskDataResult, 1, false);
        assertTask3IsPresent(taskDataResult, 2, false);
    }
}
