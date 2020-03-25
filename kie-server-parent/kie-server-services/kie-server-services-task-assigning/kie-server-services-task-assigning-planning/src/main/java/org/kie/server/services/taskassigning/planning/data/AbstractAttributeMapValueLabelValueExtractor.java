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
import java.util.Set;

public abstract class AbstractAttributeMapValueLabelValueExtractor<M extends Map<String, ?>, T> extends AbstractLabelValueExtractor<T> {

    private String attributeName;

    protected AbstractAttributeMapValueLabelValueExtractor(String attributeName, Class<T> type, String labelName, int priority) {
        super(type, labelName, priority);
        this.attributeName = attributeName;
    }

    protected abstract M getAttributes(T source);

    public String getAttributeName() {
        return attributeName;
    }

    protected abstract Set<Object> extractFromAttribute(Object attributeValue);

    @Override
    public Set<Object> extract(T source) {
        M attributes = getAttributes(source);
        return extractFromAttribute(attributes != null ? attributes.get(getAttributeName()) : null);
    }
}
