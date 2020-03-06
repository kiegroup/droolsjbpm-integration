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

package org.kie.server.api.model.taskassigning.data;

import java.util.Set;

/**
 * Defines the contract for being able to extract a Set of values from an Object instance of T at processing time and
 * declaring to which label the extracted Set must be associated to.
 */
public interface LabelValueExtractor<T> {

    /**
     * @return The source object's class from which the label value will be extracted.
     */
    Class<T> getType();

    /**
     * @return the name of the label for which the value will be extracted.
     */
    String getLabelName();

    /**
     * @return the priority for the given extractor. In cases where two extractors are defined for the same labelName
     * the one with the highest priority number will apply.
     */
    int getPriority();

    /**
     * Extracts the set for values for the given labelName from the source object.
     * @param source the object containing the values to extract.
     * @return a set with the extracted values.
     */
    Set<Object> extract(T source);
}
