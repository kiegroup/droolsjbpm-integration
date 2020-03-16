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

public class TaskAssigningTaskDataRawQueryMapperTest extends TaskAssigningTaskDataWithPotentialOwnersRawQueryMapperTest {

    @Override
    protected TaskAssigningTaskDataWithPotentialOwnersRawQueryMapper createQueryMapper() {
        return TaskAssigningTaskDataRawQueryMapper.get();
    }

    @Override
    protected String getExpectedName() {
        return TaskAssigningTaskDataRawQueryMapper.NAME;
    }

    @Override
    protected boolean readPotentialOwnersExpectedValue() {
        return false;
    }

    @Override
    protected void verifyResult(List<?> result) {
        List<List<Object>> rawResult = (List<List<Object>>) result;
        assertTask1IsPresent(rawResult, 0, false);
        assertTask2IsPresent(rawResult, 1, false);
        assertTask3IsPresent(rawResult, 2, false);
    }
}
