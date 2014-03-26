package org.kie.services.remote.rest.async;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.junit.After;
import org.junit.Test;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.remote.rest.async.AsyncDeploymentJobExecutor.JobId;
import org.kie.services.remote.rest.async.AsyncDeploymentJobExecutor.JobType;

public class AsyncDeploymentJobExecutorTest extends AbstractAsyncDeploymentJobExecutorTest {

    @After
    public void after() throws Exception {
        Thread.sleep(1000);
    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    private void addJob(int id, Map<JobId, Future<Boolean>> jobs) { 
        TestSemaphoreCallable testCallable = new TestSemaphoreCallable(id);
        Future<Boolean> job = asyncDeploymentJobExecutor.executor.submit(testCallable);
        jobs.put(new JobId(String.valueOf(id), ( id % 2 == 0 ? JobType.UNDEPLOY : JobType.DEPLOY)), job);
        submittedPendingJobsTrackerList.add(String.valueOf(id).intern());
    }

    @Test
    public void tooManyJobsTest() throws Exception {
        assertEquals("Job queue size has not been correctly set", testJobQueueSize, asyncDeploymentJobExecutor.getMaxJobQueueSize());

        // setup
        Queue<KModuleDeploymentUnit> testDepUnits = new LinkedList<KModuleDeploymentUnit>();
        float verIndex = 1.0f;
        for (int i = 0; i < 3 * testJobQueueSize; ++i) {
            String ver = new Float(verIndex++).toString().intern();
            testDepUnits.add(createDeploymentUnit("org", "art", ver));
        }

        // Fill job queue
        int submittedPendingJobs = 0;
        for (int i = 0; i < testJobQueueSize; ++i) {
            submitJob(testDepUnits.poll());
            ++submittedPendingJobs;
        }
        logger.debug(submittedPendingJobsTrackerList.size() + " deployments waiting in a queue of " + testJobQueueSize);

        // Let the first job complete
        jobCompletionSemaphore.release();
        --submittedPendingJobs;
        
        // Add another job
        submitJob(testDepUnits.poll());
        ++submittedPendingJobs;
        // This job will not be accepted
        submitJob(testDepUnits.poll());
        ++submittedPendingJobs;
       
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
        
        // Only the one job that wasn't accepted should be on this list
        assertEquals( "Not enough jobs completed.", submittedPendingJobs, submittedPendingJobsTrackerList.size() );
    }

    @Test
    public void duplicateJobAfterJobIsCompletedTest() throws Exception {
        // setup
        Queue<KModuleDeploymentUnit> testDepUnits = new LinkedList<KModuleDeploymentUnit>();
        float verIndex = 1.0f;
        for (int i = 0; i < 3 * testJobQueueSize; ++i) {
            String ver = new Float(verIndex++).toString().intern();
            testDepUnits.add(createDeploymentUnit("org", "test", ver));
        }

        // fill job queue
        int leftOverJobs = 0;
        KModuleDeploymentUnit firstDepUnit = testDepUnits.peek();
        for (int i = 0; i < testJobQueueSize; ++i) {
            submitJob(testDepUnits.poll());
        }
        // try to add a duplicate of the first job, this fails
        submitJob(firstDepUnit);
        ++leftOverJobs;

        // Let jobs complete
        for (int i = 0; i < testJobQueueSize; ++i) {
            jobCompletionSemaphore.release();
            Thread.yield();
        }

        int waitTries = 0;
        while(submittedPendingJobsTrackerList.size() != leftOverJobs && waitTries++ < maxWaitTries) {
            Thread.sleep(100);
        }
        assertEquals("Too many or not enough jobs have completed", leftOverJobs, submittedPendingJobsTrackerList.size());

        // original is done, completes
        submitJob(firstDepUnit);
        // pending original, fails
        submitJob(firstDepUnit);
        ++leftOverJobs;
        jobCompletionSemaphore.release();
        waitTries = 0;
        while(submittedPendingJobsTrackerList.size() != leftOverJobs && waitTries++ < maxWaitTries) {
            Thread.sleep(100);
        }
        assertEquals("Duplicate of completed job should have succeeded (but duplicate of pending should have failed).", 
                leftOverJobs, submittedPendingJobsTrackerList.size());
    }

    @Test
    public void jobQueueEvictionTest() throws Exception {
        Map<JobId, Future<Boolean>> jobs = asyncDeploymentJobExecutor.jobs;
        assertEquals(0, jobs.size());

        // Fill jobs queue with unfinished jobs
        int unfinishedJobs = 0;
        for (int i = 0; i < testJobQueueSize; ++i) {
            addJob(i, jobs);
            ++unfinishedJobs;
        }
        assertEquals("Jobs cache should be full.", testJobQueueSize, jobs.size());

        // Add two more (unfinished) job than the "max size" of the queue
        addJob(testJobQueueSize+1, jobs);
        ++unfinishedJobs;
        addJob(testJobQueueSize+2, jobs);
        ++unfinishedJobs;
        assertEquals("Jobs cache should NOT evict unfinished jobs.", testJobQueueSize + 2, jobs.size());

        // Finish all jobs except for 3 (last from fill queue job, 2 from +2 unfinished)
        for (int i = 0; i < testJobQueueSize - 1; ++i) {
            jobCompletionSemaphore.release();
            --unfinishedJobs;
            Thread.yield();
        }
        int testTries = 0;
        while( submittedPendingJobsTrackerList.size() > 2 && testTries++ < maxWaitTries ) {
            Thread.sleep(100);
        }
        
        // Add another unfinished jobs
        addJob(testJobQueueSize+3, jobs);
        ++unfinishedJobs;

        int jobsWaiting = 0;
        Iterator<Entry<JobId, Future<Boolean>>> jobsIter = jobs.entrySet().iterator();
        while (jobsIter.hasNext()) {
            Entry<JobId, Future<Boolean>> jobsEntry = jobsIter.next();
            if (! jobsEntry.getValue().isDone() ) {
                ++jobsWaiting;
                logger.debug( "NOT DONE: " + jobsEntry.getKey() );
            }
        }
        assertEquals("Jobs cache should only evict finished jobs.", unfinishedJobs, jobsWaiting);
    }

    @Test
    public void jobQueueTest() throws Exception { 
        // setup
        Queue<KModuleDeploymentUnit> testDepUnits = new LinkedList<KModuleDeploymentUnit>();
        float verIndex = 1.0f;
        for (int i = 0; i < testJobQueueSize+1; ++i) {
            String ver = new Float(verIndex++).toString().intern();
            testDepUnits.add(createDeploymentUnit("org", "art", ver));
        }
        
        // fill job queue
        Iterator<KModuleDeploymentUnit> iter = testDepUnits.iterator();
        for (int i = 0; i < testJobQueueSize; ++i) {
            KModuleDeploymentUnit depUnit = iter.next();
            logger.debug("[" + depUnit.getIdentifier() + "] job submitted.");
            submitJob(depUnit);
        }
        assertEquals( "Submitted jobs: ", asyncDeploymentJobExecutor.jobs.size(), testJobQueueSize );
        
        // Let all jobs finish 
        for (int i = 0; i < testJobQueueSize; ++i) {
            jobCompletionSemaphore.release();
            Thread.yield();
        }
        
        int testTries = 0;
        while( submittedPendingJobsTrackerList.size() > 0 && testTries++ < maxWaitTries ) {
            Thread.sleep(100);
        }
        assertTrue( "Still " + submittedPendingJobsTrackerList.size() + " jobs incomplete.", submittedPendingJobsTrackerList.isEmpty());
        
        // Add duplicates to the job
        iter = testDepUnits.iterator();
        for (int i = 0; i < testJobQueueSize; ++i) {
            KModuleDeploymentUnit depUnit = iter.next();
            logger.debug("[" + depUnit.getIdentifier() + "] job submitted.");
            submitJob(depUnit);
        }
        assertEquals( "Jobs submitted: ", submittedPendingJobsTrackerList.size(), testJobQueueSize);
        
        // Let duplicate jobs finish 
        for (int i = 0; i < testJobQueueSize; ++i) {
            jobCompletionSemaphore.release();
            Thread.yield();
        }

        // verify that duplicates also completed
        testTries = 0;
        while( submittedPendingJobsTrackerList.size() > 0 && testTries++ < maxWaitTries ) {
            Thread.sleep(100);
        }
        assertTrue( "Still " + submittedPendingJobsTrackerList.size() + " jobs incomplete.", submittedPendingJobsTrackerList.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getDeploymentStatusTest() throws Exception {
        Future<Boolean> doneSuccessJob = mock(Future.class);
        Future<Boolean> doneFailJob = mock(Future.class);
        Future<Boolean> doneNullJob = mock(Future.class);
        Future<Boolean> runningJob = mock(Future.class);
             
        // finished successfully
        doReturn(true).when(doneSuccessJob).isDone();
        doReturn(true).when(doneSuccessJob).get(anyLong(), any(TimeUnit.class));
        // finished failure
        doReturn(true).when(doneFailJob).isDone();
        doReturn(false).when(doneFailJob).get(anyLong(), any(TimeUnit.class));
        assertFalse( "Mock is not working!", doneFailJob.get(1, TimeUnit.MINUTES) );
        // isDone() but .get() returns null
        doReturn(true).when(doneNullJob).isDone();
        doReturn(null).when(doneNullJob).get(anyLong(), any(TimeUnit.class));
        // ! isDone()
        doReturn(false).when(runningJob).isDone();
        doReturn(null).when(doneNullJob).get(anyLong(), any(TimeUnit.class));
                
        String depId = "A";
        String otherDepId = "B";
       
        // no status
        JaxbDeploymentStatus 
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.NONEXISTENT, status);
       
        // no status, other jobs
        JobId jobId = new JobId(otherDepId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneSuccessJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.NONEXISTENT, status);
        
        // only 1 done job
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneSuccessJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYED, status);
        
        // multiple done jobs
        jobId = new JobId(depId, JobType.UNDEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneSuccessJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.UNDEPLOYED, status);
      
        // more done jobs
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneFailJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOY_FAILED, status);
        
        // more done jobs
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneSuccessJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYED, status);
        
        // Running job added
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, runningJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYING, status);
       
        // Added another running job, but it should be ignored
        jobId = new JobId(depId, JobType.UNDEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, runningJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYING, status);
        
        // Add a completed job, which should be ignored, because there's a running job before it 
        // (This is impossible, but just double-checking.. )
        jobId = new JobId(depId, JobType.UNDEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneFailJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYING, status);
       
        
        // New round of tests
        asyncDeploymentJobExecutor.jobs.clear();
       
        // one running job, no completed jobs
        jobId = new JobId(depId, JobType.UNDEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, runningJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.UNDEPLOYING, status);
        
        // two running/queued jobs, no completed jobs, should take 1rst running job
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, runningJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.UNDEPLOYING, status);
        
        // two running/queued jobs, and a complete job after that
        // (not possible, double checking )
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneSuccessJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.UNDEPLOYING, status);
        
        
        // New round of tests
        asyncDeploymentJobExecutor.jobs.clear();
      
        // only 1 done job, with null status
        jobId = new JobId(depId, JobType.DEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneNullJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYING, status);
        
        // one running job after the 1 done job, with null status
        jobId = new JobId(depId, JobType.UNDEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, runningJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.DEPLOYING, status);

        
        // New round of tests
        asyncDeploymentJobExecutor.jobs.clear();
     
        // failed done undeploy job
        jobId = new JobId(depId, JobType.UNDEPLOY);
        asyncDeploymentJobExecutor.jobs.put(jobId, doneFailJob);
        status = asyncDeploymentJobExecutor.getStatus(depId);
        assertEquals( JaxbDeploymentStatus.UNDEPLOY_FAILED, status);
    }
    
}