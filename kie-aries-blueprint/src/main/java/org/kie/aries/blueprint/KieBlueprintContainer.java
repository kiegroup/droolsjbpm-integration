/*
 * Copyright 2013 JBoss Inc
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
package org.kie.aries.blueprint;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.apache.aries.blueprint.container.SimpleNamespaceHandlerSet;
import org.apache.aries.blueprint.parser.NamespaceHandlerSet;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KieBlueprintContainer extends BlueprintContainerImpl {
    public KieBlueprintContainer(ClassLoader loader, List<URL> resources) throws Exception {
        super(loader, resources);
    }

    public KieBlueprintContainer(ClassLoader loader, List<URL> resources, boolean init) throws Exception {
        super(loader, resources, init);
    }

    public KieBlueprintContainer(ClassLoader loader, List<URL> resources, Map<String, String> properties, boolean init) throws Exception {
        super(loader, resources, properties, init);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    protected NamespaceHandlerSet createNamespaceHandlerSet(Set<URI> namespaces) {
        NamespaceHandlerSet handlerSet = new SimpleNamespaceHandlerSet();
        try {
            URI namespaceURL = URI.create("http://drools.org/schema/kie-aries-blueprint");
            URL schemaURL = getResource("org/kie/aries/blueprint/kie-aries-blueprint.xsd");
            KieNamespaceHandler namespaceHandler = new KieNamespaceHandler();
            ((SimpleNamespaceHandlerSet)handlerSet).addNamespace(namespaceURL, schemaURL, namespaceHandler);
        } catch(Exception e) {
            e.printStackTrace();
        }
        // Check namespaces
        Set<URI> unsupported = new LinkedHashSet<URI>();
        for (URI ns : namespaces) {
            if (!handlerSet.getNamespaces().contains(ns)) {
                unsupported.add(ns);
            }
        }
        if (unsupported.size() > 0) {
            throw new IllegalArgumentException("Unsupported namespaces: " + unsupported.toString());
        }
        return handlerSet;
    }
}
