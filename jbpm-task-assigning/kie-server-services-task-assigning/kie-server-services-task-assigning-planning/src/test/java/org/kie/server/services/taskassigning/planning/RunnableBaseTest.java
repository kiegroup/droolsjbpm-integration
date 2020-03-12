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

package org.kie.server.services.taskassigning.planning;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public abstract class RunnableBaseTest<T extends RunnableBase> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected static final long TEST_TIMEOUT = 5000;

    protected T runnableBase;

    @Before
    public void setUp() {
        runnableBase = createRunnableBase();
    }

    protected abstract T createRunnableBase();

    protected CompletableFuture<Void> startRunnableBase() {
        return CompletableFuture.runAsync(runnableBase);
    }

    @Test(timeout = TEST_TIMEOUT)
    public void destroy() throws Exception {
        CompletableFuture future = startRunnableBase();
        assertTrue(runnableBase.isAlive());
        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }
}
