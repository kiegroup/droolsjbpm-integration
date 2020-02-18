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
import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.client.TaskAssigningRuntimeClient;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SolutionProcessorTest extends RunnableBaseTest<SolutionProcessor> {

    private static final String TARGET_USER_ID = "TARGET_USER_ID";
    private static final int PUBLISH_WINDOW_SIZE = 2;

    private TaskAssigningRuntimeDelegate delegate;

    @Mock
    private TaskAssigningRuntimeClient runtimeClient;

    @Mock
    private PlanningExecutionResult result;

    private SolutionProcessor.Result capturedResult;

    @Mock
    private List<PlanningItem> generatedPlan;

    private CountDownLatch processingFinished = new CountDownLatch(1);

    @Override
    protected SolutionProcessor createRunnableBase() {
        delegate = spy(new TaskAssigningRuntimeDelegate(runtimeClient));
        SolutionProcessor processor = spy(new SolutionProcessor(delegate,
                                                                result -> {
                                                                    SolutionProcessorTest.this.capturedResult = result;
                                                                    // emulate the processing finalization.
                                                                    processingFinished.countDown();
                                                                },
                                                                TARGET_USER_ID, PUBLISH_WINDOW_SIZE));
        doReturn(generatedPlan).when(processor).buildPlanning(any(), anyInt());
        return processor;
    }

    @Test(timeout = TEST_TIMEOUT)
    public void process() throws Exception {
        CompletableFuture future = startRunnableBase();

        doReturn(result).when(delegate).executePlanning(generatedPlan, TARGET_USER_ID);

        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());
        runnableBase.process(solution);

        // wait until the processing has finished
        processingFinished.await();

        verify(delegate).executePlanning(generatedPlan, TARGET_USER_ID);
        assertEquals(result, capturedResult.getExecutionResult());

        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }

    @Test
    public void isProcessing() {
        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());
        runnableBase.process(solution);
        assertTrue(runnableBase.isProcessing());
    }

    @Test
    public void processWithInvalidStatusFailure() {
        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());
        runnableBase.process(solution);
        Assertions.assertThatThrownBy(() -> runnableBase.process(solution))
                .hasMessage("SolutionProcessor process method can only be invoked when the status is STOPPED");
    }

    @Test(timeout = TEST_TIMEOUT)
    public void processWithDelegateError() throws Exception {
        CompletableFuture future = startRunnableBase();

        TaskAssigningSolution solution = new TaskAssigningSolution(-1, new ArrayList<>(), new ArrayList<>());

        RuntimeException generatedError = new RuntimeException("Emulate a service invocation error.");
        doThrow(generatedError).when(delegate).executePlanning(generatedPlan, TARGET_USER_ID);

        runnableBase.process(solution);

        // wait until the processing has finished
        processingFinished.await();

        assertEquals(generatedError, capturedResult.getException());
        runnableBase.destroy();
        future.get();
        assertTrue(runnableBase.isDestroyed());
    }
}
