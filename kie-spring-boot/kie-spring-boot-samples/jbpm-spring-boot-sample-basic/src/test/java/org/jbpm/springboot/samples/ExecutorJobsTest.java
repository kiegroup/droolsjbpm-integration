/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jbpm.executor.AsynchronousJobEvent;
import org.jbpm.executor.AsynchronousJobListener;
import org.jbpm.executor.commands.LogCleanupCommand;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.runtime.query.QueryContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-executor.properties")
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class ExecutorJobsTest {
    
    @Autowired
    private ExecutorService executorService;
    
    @Before
    public void configure() {
        executorService.init();
    }
    
    @After
    public void close() {
        executorService.destroy();
    }
             
    @Test
    public void testLogCleanupCommand() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        ((ExecutorServiceImpl) executorService).addAsyncJobListener(new AsynchronousJobListener() {
            
            @Override
            public void beforeJobScheduled(AsynchronousJobEvent event) {                
            }
            
            @Override
            public void beforeJobExecuted(AsynchronousJobEvent event) {                
            }
            
            @Override
            public void beforeJobCancelled(AsynchronousJobEvent event) {                
            }
            
            @Override
            public void afterJobScheduled(AsynchronousJobEvent event) {                
            }
            
            @Override
            public void afterJobExecuted(AsynchronousJobEvent event) {
                latch.countDown();
            }
            
            @Override
            public void afterJobCancelled(AsynchronousJobEvent event) {                
            }
        });
        
        CommandContext commandContext = new CommandContext();
        commandContext.setData("EmfName", "org.jbpm.domain");
        commandContext.setData("SkipProcessLog", "false");
        commandContext.setData("SkipTaskLog", "false");
        commandContext.setData("SkipExecutorLog","false");
        commandContext.setData("SingleRun", "true");
        
        long jobId = executorService.scheduleRequest(LogCleanupCommand.class.getName(), commandContext);        
        assertNotNull(jobId);
        
        latch.await(10, TimeUnit.SECONDS);
        
        List<RequestInfo> completed = executorService.getCompletedRequests(new QueryContext());
        assertEquals(1, completed.size());
    }
    
    
}

