package org.kie.services.remote.rest.async;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.jboss.resteasy.logging.Logger;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.internal.deployment.DeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.remote.rest.async.AsyncDeploymentJobExecutor.JobType;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AbstractAsyncDeploymentJobExecutorTest {

    protected final static Logger logger = Logger.getLogger(AbstractAsyncDeploymentJobExecutorTest.class);

    protected AsyncDeploymentJobExecutor asyncDeploymentJobExecutor = null;
    protected KModuleDeploymentService deploymentService = null;

    protected final Semaphore jobCompletionSemaphore = new Semaphore(0, true);
    protected Queue<String> submittedPendingJobsTrackerList = new ConcurrentLinkedQueue<String>();

    protected final static int testJobQueueSize = 3;
    protected final static int maxWaitTries = 5;

    @BeforeClass
    public static void beforeClass() throws Exception {
        try {
            Field queueSizePropField = AsyncDeploymentJobExecutor.class.getDeclaredField("MAX_JOB_QUEUE_SIZE_PROP");
            queueSizePropField.setAccessible(true);
            String propName = (String) queueSizePropField.get(null);
            System.setProperty(propName, String.valueOf(testJobQueueSize));
        } catch (NoSuchFieldException nsfe) {
            Field queueSizeField = AsyncDeploymentJobExecutor.class.getDeclaredField("maxQueueSize");
            queueSizeField.setAccessible(true);
            queueSizeField.set(null, testJobQueueSize);
        }
    }

    @Before
    public void before() {
        asyncDeploymentJobExecutor = new AsyncDeploymentJobExecutor();

        deploymentService = mock(KModuleDeploymentService.class);
        Answer<Void> semaphoreAnswer = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // wait
                jobCompletionSemaphore.acquire();

                // inform that you're (almost) done
                KModuleDeploymentUnit depUnit = (KModuleDeploymentUnit) invocation.getArguments()[0];
                String op = invocation.getMethod().getName();
                logger.debug("(Finished mock " + op + " of [" + depUnit.getIdentifier() + "])");

                // remove from tracker
                String ver = depUnit.getVersion();
                submittedPendingJobsTrackerList.remove(ver);

                return null;
            }
        };

        doAnswer(semaphoreAnswer).when(deploymentService).deploy(any(DeploymentUnit.class));
        doAnswer(semaphoreAnswer).when(deploymentService).undeploy(any(DeploymentUnit.class));
    }

    // HELPER METHODS -------------------------------------------------------------------------------------------------------------

    protected JaxbDeploymentJobResult submitJob(KModuleDeploymentUnit depUnit) {
        submittedPendingJobsTrackerList.add(depUnit.getVersion().intern());
        return asyncDeploymentJobExecutor.submitJob(deploymentService, depUnit, JobType.DEPLOY);
    }

    protected KModuleDeploymentUnit createDeploymentUnit(String groupId, String artifactId, String version) {
        KModuleDeploymentUnit depUnit = new KModuleDeploymentUnit(groupId, artifactId, version);
        return depUnit;
    }
    
    // HELPER CLASSES -------------------------------------------------------------------------------------------------------------
    
    protected class TestSemaphoreCallable implements Callable<Boolean> {

        public Semaphore jobSemaphore = jobCompletionSemaphore;
        private final int id; 
        
        public TestSemaphoreCallable(int i, Semaphore newSemaphore) { 
            this.id = i;
            if( newSemaphore != null ) { 
                this.jobSemaphore = newSemaphore;
            }
        }
        
        public TestSemaphoreCallable(int i) { 
            this(i, null);
        }
        
        @Override
        public Boolean call() throws Exception {
            jobSemaphore.acquire();
            logger.debug( id + " completed" );
            submittedPendingJobsTrackerList.remove(String.valueOf(this.id).intern());
            return true;
        }
    }
    
    protected class TestSubmitCallable implements Callable<JaxbDeploymentJobResult> {

        private final KModuleDeploymentUnit depUnit;
        
        public TestSubmitCallable(KModuleDeploymentUnit depUnit) { 
            this.depUnit = depUnit;
        }
        
        @Override
        public JaxbDeploymentJobResult call() throws Exception {
            submittedPendingJobsTrackerList.add(depUnit.getVersion().intern());
            return asyncDeploymentJobExecutor.submitJob(deploymentService, depUnit, JobType.DEPLOY);
        }
        
    }
}
