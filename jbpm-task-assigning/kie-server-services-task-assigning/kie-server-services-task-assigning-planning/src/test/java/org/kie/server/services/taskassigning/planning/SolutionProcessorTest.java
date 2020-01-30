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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.server.api.model.taskassigning.ExecutePlanningResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.client.TaskAssigningRuntimeClient;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolutionProcessorTest extends RunnableBaseTest<SolutionProcessor> {

    private static final String TARGET_USER_ID = "TARGET_USER_ID";
    private static final int PUBLISH_WINDOW_SIZE = 2;

    private TaskAssigningRuntimeDelegate delegate;

    @Mock
    private TaskAssigningRuntimeClient runtimeClient;

    @Mock
    private Consumer<SolutionProcessor.Result> resultConsumer;

    @Mock
    private ExecutePlanningResult result;

    @Captor
    private ArgumentCaptor<SolutionProcessor.Result> resultCaptor;

    @Mock
    private List<PlanningItem> generatedPlan;

    @Override
    protected SolutionProcessor createRunnableBase() {
        delegate = spy(new TaskAssigningRuntimeDelegateMock(runtimeClient));
        SolutionProcessor processor = spy(new SolutionProcessor(delegate, resultConsumer, TARGET_USER_ID, PUBLISH_WINDOW_SIZE));
        doReturn(generatedPlan).when(processor).buildPlanning(any(), anyInt());
        return processor;
    }

    @Test(timeout = TEST_TIMEOUT)
    public void process() throws Exception {
        CompletableFuture future = startRunnableBase();
        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());
        runnableBase.process(solution);
        assertTrue(runnableBase.isProcessing());

        // wait while the processing is occurring.
        while (runnableBase.isProcessing()) {
            Thread.sleep(100);
        }

        verify(delegate).executePlanning(generatedPlan, TARGET_USER_ID);
        verify(resultConsumer).accept(resultCaptor.capture());
        assertEquals(result, resultCaptor.getValue().getExecuteResult());

        runnableBase.destroy();
        assertTrue(runnableBase.isDestroyed());
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    public void processWithFailure() throws Exception {
        CompletableFuture future = startRunnableBase();
        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());
        runnableBase.process(solution);
        Assertions.assertThatThrownBy(() -> runnableBase.process(solution))
                .hasMessage("SolutionProcessor process method can only be invoked when the status is STOPPED");

        runnableBase.destroy();
        assertTrue(runnableBase.isDestroyed());
        future.get();
    }

    @Test(timeout = TEST_TIMEOUT)
    public void processWithDelegateError() throws Exception {
        CompletableFuture future = startRunnableBase();
        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());

        RuntimeException generatedError = new RuntimeException("Emulate a service invocation error.");
        when(delegate.executePlanning(generatedPlan, TARGET_USER_ID)).thenThrow(generatedError);

        runnableBase.process(solution);

        // wait while the processing is occurring.
        while (runnableBase.isProcessing()) {
            Thread.sleep(100);
        }

        verify(resultConsumer).accept(resultCaptor.capture());
        assertTrue(resultCaptor.getValue().hasException());
        assertEquals(generatedError, resultCaptor.getValue().getException());

        runnableBase.destroy();
        assertTrue(runnableBase.isDestroyed());
        future.get();
    }

    private class TaskAssigningRuntimeDelegateMock extends TaskAssigningRuntimeDelegate {

        public TaskAssigningRuntimeDelegateMock(TaskAssigningRuntimeClient runtimeClient) {
            super(runtimeClient);
        }

        @Override
        public ExecutePlanningResult executePlanning(List<PlanningItem> planningItems, String userId) {
            try {
                // emulate some time to finish
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.debug(e.getMessage());
            }
            return result;
        }
    }
}
