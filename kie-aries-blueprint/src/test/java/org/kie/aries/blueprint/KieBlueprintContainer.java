/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
import org.apache.aries.blueprint.reflect.PassThroughMetadataImpl;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KieBlueprintContainer extends BlueprintContainerImpl {

    public KieBlueprintContainer(ClassLoader loader, List<URL> resources) throws Exception {
        this(loader, resources, null, true);
    }

    public KieBlueprintContainer(ClassLoader loader, List<URL> resources, boolean init) throws Exception {
        this(loader, resources, null, init);
    }

    public KieBlueprintContainer(ClassLoader loader, List<URL> resources, Map<String, String> properties, boolean init) throws Exception {
        super(loader, resources, properties, createKieNamespaceHandlerSet(loader), false);
        // kie-aries-blueprint relies on the following for retrieving bundle classloader [BZ-1310039]
        getComponentDefinitionRegistry().registerComponentDefinition(new PassThroughMetadataImpl("blueprintBundleContext", createMockBundleContext(loader)));
        // the initialization must happen after blueprintBundleContext is registered
        if (init) {
            super.init();
        }
    }

    private static NamespaceHandlerSet createKieNamespaceHandlerSet(ClassLoader classloader) {
        NamespaceHandlerSet handlerSet = new SimpleNamespaceHandlerSet();
        URI namespaceURL = URI.create("http://drools.org/schema/kie-aries-blueprint/1.0.0");
        URL schemaURL = classloader.getResource("org/kie/aries/blueprint/kie-aries-blueprint.xsd");
        KieNamespaceHandler namespaceHandler = new KieNamespaceHandler();
        ((SimpleNamespaceHandlerSet) handlerSet).addNamespace(namespaceURL, schemaURL, namespaceHandler);
        return handlerSet;
    }

    private static BundleContext createMockBundleContext(ClassLoader classLoader) {
        BundleContext mockBundleContext = Mockito.mock(BundleContext.class);
        Bundle mockBundle = createMockBundle(classLoader);
        Mockito.when(mockBundleContext.getBundle()).thenReturn(mockBundle);
        return mockBundleContext;
    }

    private static Bundle createMockBundle(ClassLoader classLoader) {
        Bundle mockBundle = Mockito.mock(Bundle.class);
        BundleWiring mockBundleWiring = createMockBundleWiring(classLoader);
        Mockito.when(mockBundle.adapt(Matchers.eq(BundleWiring.class))).thenReturn(mockBundleWiring);
        return mockBundle;
    }

    private static BundleWiring createMockBundleWiring(ClassLoader classLoader) {
        BundleWiring mockBundleWiring = Mockito.mock(BundleWiring.class);
        Mockito.when(mockBundleWiring.getClassLoader()).thenReturn(classLoader);
        return mockBundleWiring;
    }

    public void registerBean(String name, Object bean) {
        getRepository().addFullObject( name, new CompletedFuture<Object>(bean));
    }

    public static class CompletedFuture<T> implements Future<T> {

        private final T result;

        public CompletedFuture( T result ) {
            this.result = result;
        }

        @Override
        public boolean cancel( boolean mayInterruptIfRunning ) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public T get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            return result;
        }
    }
}
