/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.aries.blueprint.namespace;

import org.osgi.service.blueprint.container.BlueprintContainer;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlueprintContextHelper {
    private final BlueprintContainer blueprintContainer;
    private final Object blueprintContext;
    private final Method addObjectMethod;

    public BlueprintContextHelper(BlueprintContainer blueprintContainer) {
        this.blueprintContainer = blueprintContainer;

        // The ExecutionContext class is in the blueprint-core module, but we cannot explicitly import it
        // because there are 2 implementations of it (osgi and no-osgi) with same class names and we
        // dynamically import one of the 2. For this reason ExecutionContext class can be used only via reflection

        try {
            Class<?> ctxClass = Class.forName( "org.apache.aries.blueprint.di.ExecutionContext", true, blueprintContainer.getClass().getClassLoader() );
            addObjectMethod = ctxClass.getMethod( "addFullObject", String.class, Future.class );

            Class<?> holderClass = Class.forName( "org.apache.aries.blueprint.di.ExecutionContext$Holder", true, blueprintContainer.getClass().getClassLoader() );
            Method getCtxMethod = holderClass.getMethod( "getContext" );
            blueprintContext = getCtxMethod.invoke( null );
        } catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public void registerBean(String name, Object bean) {
        try {
            addObjectMethod.invoke( blueprintContext, name, new CompletedFuture<Object>( bean ) );
        } catch (Exception e) {
            throw new RuntimeException( e );
        }
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
