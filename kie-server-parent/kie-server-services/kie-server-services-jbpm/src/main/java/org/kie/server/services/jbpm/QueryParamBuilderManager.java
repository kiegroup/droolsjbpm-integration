/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jbpm.services.api.query.QueryParamBuilderFactory;

public class QueryParamBuilderManager {

    private static QueryParamBuilderManager INSTANCE = new QueryParamBuilderManager();

    private ConcurrentMap<String, List<QueryParamBuilderFactory>> factoriesPerContainer = new ConcurrentHashMap<String, List<QueryParamBuilderFactory>>();
    private List<QueryParamBuilderFactory> factories = new CopyOnWriteArrayList<QueryParamBuilderFactory>();

    private QueryParamBuilderManager() {

    }

    public static QueryParamBuilderManager get() {
        return INSTANCE;
    }

    public void discoverAndAddQueryFactories(String containerId, ClassLoader cl) {
        List<QueryParamBuilderFactory> added = new ArrayList<QueryParamBuilderFactory>();

        ServiceLoader<QueryParamBuilderFactory> availableFactories = ServiceLoader.load(QueryParamBuilderFactory.class, cl);
        for (QueryParamBuilderFactory factory : availableFactories) {

            added.add(factory);
        }
        if (!added.isEmpty()) {
            factoriesPerContainer.putIfAbsent(containerId, added);
            factories.addAll(added);
        }

    }

    public void removeQueryFactories(String containerId) {
        List<QueryParamBuilderFactory> removed = factoriesPerContainer.remove(containerId);
        if (removed != null && !removed.isEmpty()) {
            factories.removeAll(removed);
        }
    }

    public QueryParamBuilderFactory find(String identifier) {
        for (QueryParamBuilderFactory factory : factories) {
            if (factory.accept(identifier)) {
                return factory;
            }
        }

        return null;
    }
}
