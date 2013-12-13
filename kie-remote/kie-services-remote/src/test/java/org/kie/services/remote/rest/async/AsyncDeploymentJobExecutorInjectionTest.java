package org.kie.services.remote.rest.async;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BMUnitRunner.class)
public class AsyncDeploymentJobExecutorInjectionTest extends AbstractAsyncDeploymentJobExecutorTest {

    final ExecutorService executor = Executors.newCachedThreadPool();
    
    @Test
    @BMScript(value="cancel-job", dir="byteman")
    public void staggeredDuplicateSubmission() throws Exception { 
        
        Queue<KModuleDeploymentUnit> testDepUnits = new LinkedList<KModuleDeploymentUnit>();
        float verIndex = 1.0f;
        for (int i = 0; i < testJobQueueSize; ++i) {
            String ver = new Float(verIndex++).toString().intern();
            testDepUnits.add(createDeploymentUnit("org", "art", ver));
        }

        // Fill job queue
        int submittedPendingJobs = 0;
        for (int i = 0; i < testJobQueueSize; ++i) {
            KModuleDeploymentUnit depUnit = testDepUnits.poll();
            executor.submit(new TestSubmitCallable(depUnit));
            ++submittedPendingJobs;
        }
     
        for (int i = 0; i < testJobQueueSize; ++i) {
            jobCompletionSemaphore.release();
            Thread.yield();
            --submittedPendingJobs;
        }
        
        int tries = 0;
        while (submittedPendingJobsTrackerList.size() > submittedPendingJobs && tries++ < maxWaitTries) {
            logger.debug((submittedPendingJobsTrackerList.size() - submittedPendingJobs) + " deployments more to process.");
            Thread.sleep(100);
        }
        
        assertEquals( "Not enough jobs completed.", submittedPendingJobs, submittedPendingJobsTrackerList.size() );
    }
}
