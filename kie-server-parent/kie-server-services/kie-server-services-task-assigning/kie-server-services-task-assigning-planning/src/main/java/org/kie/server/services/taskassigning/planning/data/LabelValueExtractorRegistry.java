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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiConsumer;

import org.kie.server.api.model.taskassigning.data.LabelValueExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelValueExtractorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelValueExtractorRegistry.class);

    private static final LabelValueExtractorRegistry INSTANCE = new LabelValueExtractorRegistry();

    private Map<Class<?>, Set<LabelValueExtractor<?>>> extractorsBySourceType = new HashMap<>();

    private LabelValueExtractorRegistry() {
        registerExtractors(getClass().getClassLoader());
    }

    public static LabelValueExtractorRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void registerExtractors(ClassLoader classLoader) {
        final ServiceLoader<LabelValueExtractor> availableExtractors = ServiceLoader.load(LabelValueExtractor.class, classLoader);
        for (LabelValueExtractor extractor : availableExtractors) {
            final Set<LabelValueExtractor<?>> typeExtractors = extractorsBySourceType.computeIfAbsent(extractor.getType(), key -> new HashSet<>());
            final LabelValueExtractor previousExtractor = typeExtractors.stream()
                    .filter(previous -> previous.getLabelName().equals(extractor.getLabelName()))
                    .findFirst().orElse(null);
            if (previousExtractor != null) {
                if (previousExtractor.getPriority() < extractor.getPriority()) {
                    LOGGER.info("LabelValueExtractor with [previousType: {}, previousLabelName:{}, previousPriority: {}], will be replaced with -> [type:{}, labelName:{}, priority: {}]",
                                previousExtractor.getType(), previousExtractor.getLabelName(), previousExtractor.getPriority(),
                                extractor.getType(), extractor.getLabelName(), extractor.getPriority());
                    typeExtractors.remove(previousExtractor);
                    typeExtractors.add(extractor);
                }
            } else {
                typeExtractors.add(extractor);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<Set<LabelValueExtractor<T>>> getLabelValueExtractors(Class<T> sourceType) {
        return (Optional<Set<LabelValueExtractor<T>>>) Optional.ofNullable((T) extractorsBySourceType.get(sourceType));
    }

    public <T> void applyLabelValueExtractors(Class<T> sourceType, T source, BiConsumer<String, Set<Object>> extractedValuesConsumer) {
        final Optional<Set<LabelValueExtractor<T>>> extractorsSet = getLabelValueExtractors(sourceType);
        extractorsSet.ifPresent(extractors -> extractors.forEach(valueExtractor -> {
            final Set<Object> values = valueExtractor.extract(source);
            if (values != null) {
                extractedValuesConsumer.accept(valueExtractor.getLabelName(), values);
            }
        }));
    }
}
