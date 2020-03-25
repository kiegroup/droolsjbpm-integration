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

import org.kie.server.api.model.taskassigning.data.LabelValueExtractor;

public abstract class AbstractLabelValueExtractor<T> implements LabelValueExtractor<T> {

    private final Class<T> type;
    private final String labelName;
    private final int priority;

    protected AbstractLabelValueExtractor(final Class<T> type, final String labelName, final int priority) {
        this.type = type;
        this.labelName = labelName;
        this.priority = priority;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public String getLabelName() {
        return labelName;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}