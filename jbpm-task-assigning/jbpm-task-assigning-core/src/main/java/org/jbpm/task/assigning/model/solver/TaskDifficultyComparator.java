/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.task.assigning.model.solver;

import java.io.Serializable;
import java.util.Comparator;

import org.jbpm.task.assigning.model.Task;

public class TaskDifficultyComparator implements Comparator<Task>,
                                                 Serializable {

    private static final Comparator<Task> COMPARATOR =
            //priority goes from [0(high)... 5 (medium)... 10 (low)] so we switch factors for the comparison.
            Comparator.comparingInt((Task task) -> -task.getPriority())
                    .thenComparingLong(Task::getId);

    @Override
    public int compare(Task a, Task b) {
        return COMPARATOR.compare(a, b);
    }
}
