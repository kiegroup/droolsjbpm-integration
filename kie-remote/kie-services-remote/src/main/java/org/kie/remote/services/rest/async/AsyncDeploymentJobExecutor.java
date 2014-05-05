package org.kie.remote.services.rest.async;

import static org.kie.remote.services.rest.DeploymentResource.convertKModuleDepUnitToJaxbDepUnit;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lot of the ideas in this class have been taken from the <code>org.jboss.resteasy.coreAsynchronousDispatcher</code> class.
 * </p>
 * Unfortunately, the Resteasy asynchronous job mechanism has a number of bugs (most importantly, RESTEASY-682) which make it
 * unusable for our purposes.
 * </p>
 * Because tomcat compatibility is also important, we also can't use the EJB asynchronous mechanisms.
 * </p>
 * That leaves us with the java.util.concurrent.* executor framework. However, because the {@link KModuleDeploymentService} gives
 * no guarantee with regards to concurrency (e.g. is not thread-safe), it's important to make sure that the same deployment unit is
 * not <i>concurrently</i> deployed (or undeployed).
 * </p>
 * In order to satisfy that last requirement (no concurrent un/deployment of the same deployment unit), we use a </b>single threaded
 * exector</b>, which queues up other jobs.
 */
@ApplicationScoped
public class AsyncDeploymentJobExecutor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncDeploymentJobExecutor.class);

    private final static boolean ASYNC_DEPLOY_ENABLED = Boolean.parseBoolean(System.getProperty("kie.services.rest.deploy.async", "true"));

    final ExecutorService executor;
    final Map<JobId, Future<Boolean>> jobs;

    private static int maxQueueSize = 100;
    public static final String MAX_JOB_QUEUE_SIZE_PROP = "org.kie.remote.rest.deployment.job.queue.size";

    static enum JobType {
        DEPLOY, UNDEPLOY;
    }

    public AsyncDeploymentJobExecutor() {
        String maxCacheSizePropStr = System.getProperty(MAX_JOB_QUEUE_SIZE_PROP, String.valueOf(maxQueueSize));
        try {
            maxQueueSize = Integer.valueOf(maxCacheSizePropStr).intValue();
        } catch (NumberFormatException nfe) {
            logger.error("Unable to format " + MAX_JOB_QUEUE_SIZE_PROP + " value: '" + maxCacheSizePropStr + "', " + "using "
                    + maxQueueSize + " for job cache size");
        }
        Cache<Boolean> cache = new Cache<Boolean>(maxQueueSize);
        jobs = Collections.synchronizedMap(cache);

        // See the javadoc for this class (above)
        executor = Executors.newSingleThreadExecutor();
    }

    public JaxbDeploymentJobResult submitDeployJob(KModuleDeploymentService deploymentService, KModuleDeploymentUnit depUnit) {
        return submitJob(deploymentService, depUnit, JobType.DEPLOY);
    }

    public JaxbDeploymentJobResult submitUndeployJob(KModuleDeploymentService deploymentService, KModuleDeploymentUnit depUnit) {
        return submitJob(deploymentService, depUnit, JobType.UNDEPLOY);
    }

    JaxbDeploymentJobResult submitJob(KModuleDeploymentService deploymentService, KModuleDeploymentUnit depUnit, JobType type) {
        String typeName = type.toString();
        String typeNameLower = typeName.toLowerCase();

        if (ASYNC_DEPLOY_ENABLED) {
            logger.info("Deployment executing as async jobs");
            String loggerJobId = typeName + " job for [" + depUnit.getIdentifier() + "]";

            if (jobs.size() > maxQueueSize) {
                String msg = "Queue is full with existing incomplete un/deploy jobs";
                logger.info(loggerJobId + " NOT submitted: " + msg);
                return new JaxbDeploymentJobResult(msg, false, convertKModuleDepUnitToJaxbDepUnit(depUnit), typeName);
            }

            // Submit job
            JobId jobId = new JobId(depUnit.getIdentifier(), type);
            DeploymentJobCallable jobCallable = new DeploymentJobCallable(depUnit, type, deploymentService);
            Future<Boolean> newJob = executor.submit(jobCallable);
            jobs.put(jobId, newJob);

            logger.info(loggerJobId + " submitted succesfully");
            return new JaxbDeploymentJobResult("Deployment (" + typeNameLower + ") job submitted successfully.", true,
                    convertKModuleDepUnitToJaxbDepUnit(depUnit), typeName);
        }  else {
            logger.info("Deployment executing as part of the incoming request, async mode not available");
            String message = " completed successfully";
            boolean success = true;
            switch (type) {
                case DEPLOY:
                    try {
                        deploymentService.deploy(depUnit);
                        logger.debug("Deployment unit [" + depUnit.getIdentifier() + "] deployed");
                    } catch (Exception e) {
                        message = " failed due to " + e.getMessage();
                        success = false;
                        logger.error("Unable to deploy [" + depUnit.getIdentifier() + "]", e);
                    }
                    break;
                case UNDEPLOY:
                    try {
                        deploymentService.undeploy(depUnit);
                        logger.debug("Deployment unit [" + depUnit.getIdentifier() + "] undeployed");
                    } catch (Exception e) {
                        message = " failed due to " + e.getMessage();
                        success = false;
                        logger.error("Unable to undeploy [" + depUnit.getIdentifier() + "]", e);
                    }
                    break;
                default:
                    logger.error("Unknown " + JobType.class.getSimpleName() + " type (" + type.toString() + "), not taking any action");
            }
            return new JaxbDeploymentJobResult("Deployment (" + typeNameLower + ") job " + message, success,
                    convertKModuleDepUnitToJaxbDepUnit(depUnit), typeName);
        }
    }

    /**
     * This method returns the status of a deployment based on the (retricted size) queue of un/deploy jobs.
     * </p>
     * It is assumed that the caller was unable to find the deployment unit via normal means.
     * 
     * @param deploymentUnitId The id of the deployment
     * @return The {@link JaxbDeploymentStatus} of the deployment
     */
    public JaxbDeploymentStatus getStatus(String deploymentUnitId) {
        Entry<JobId, Future<Boolean>> currentJobEntry = null;
        Entry<JobId, Future<Boolean>> nextWaitingJobEntry = null;
        Iterator<Entry<JobId, Future<Boolean>>> iter = jobs.entrySet().iterator();
        // first -> last
        while (iter.hasNext()) {
            Entry<JobId, Future<Boolean>> entry = iter.next();
            if (entry.getKey().matches(deploymentUnitId)) {
                Future<Boolean> job = entry.getValue();
                Boolean thisJobSuccess = null;
                try {
                    thisJobSuccess = job.get(1, TimeUnit.NANOSECONDS);
                } catch (Exception e) {
                    logger.warn("Unable to retrieve status of job {}", entry.getKey());
                    // do nothing
                }
                
                // Either: 
                // 1. Grab the complete job, and the first running one after that. 
                // 2. Grab the first running job
                if (job.isDone() && thisJobSuccess != null) {
                    currentJobEntry = entry;
                } else { 
                    nextWaitingJobEntry = entry;
                    break;
                }
            }
        }

        if (currentJobEntry == null && nextWaitingJobEntry == null) {
            return JaxbDeploymentStatus.NONEXISTENT;
        } else if (currentJobEntry == null && nextWaitingJobEntry != null) {
            JobId jobId = nextWaitingJobEntry.getKey();
            if (jobId.matches(JobType.DEPLOY)) {
               return JaxbDeploymentStatus.DEPLOYING; 
            } else { 
               return JaxbDeploymentStatus.UNDEPLOYING;
            }
        } else if( currentJobEntry != null ){
            JobId jobId = currentJobEntry.getKey();
            Future<Boolean> job = currentJobEntry.getValue();
            if (nextWaitingJobEntry != null) {
                job = nextWaitingJobEntry.getValue();
            }
            Boolean jobSuccess = null;
            try {
                jobSuccess = job.get(1, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.warn("Unable to retrieve status of job {}", jobId, e);
                // Technically, this should never happen, but if it has, then there's probably something wrong.
                // It is possible that this is a false negative though..
            }

            if (jobId.matches(JobType.DEPLOY)) {
                if (job.isDone()) {
                    if (jobSuccess == null) {
                        return JaxbDeploymentStatus.DEPLOYING;
                    } else if (jobSuccess) {
                        return JaxbDeploymentStatus.DEPLOYED;
                    } else {
                        return JaxbDeploymentStatus.DEPLOY_FAILED;
                    }
                } else {
                    return JaxbDeploymentStatus.DEPLOYING;
                }
            } else {
                if (job.isDone()) {
                    if (jobSuccess == null) {
                        return JaxbDeploymentStatus.UNDEPLOYING;
                    } else if (jobSuccess) {
                        return JaxbDeploymentStatus.UNDEPLOYED;
                    } else {
                        return JaxbDeploymentStatus.UNDEPLOY_FAILED;
                    }
                } else {
                    return JaxbDeploymentStatus.UNDEPLOYING;
                }
            }
        } else { 
            throw new IllegalStateException("This block in the code should never be reached. Please contact the developers!");
        }
    }

    /**
     * The classic Java LinkedHashMap/cache implementation..
     * </p>
     * EXCEPT that the cache will not get rid of {@link Future} entries
     * which have not yet completed (Future.isDone() == false)!
     * </p>
     * This is in order to limit possible DOS attacks or otherwise overloading
     * of the server.
     * 
     * @param <T>
     */
    private static class Cache<Boolean> extends LinkedHashMap<JobId, Future<Boolean>> {
        /** default serial version id */
        private static final long serialVersionUID = 1L;
        
        private int maxSize = 100;

        public Cache(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<JobId, Future<Boolean>> stringFutureEntry) {
            Future<Boolean> future = stringFutureEntry.getValue();
            Boolean success = null;
            try {
                success = future.get(1, TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                // do nothing..
            }
            return stringFutureEntry.getValue().isDone() && (success != null) && (size() > maxSize);
        }
    }

    /**
     * The class used to identify a job (Future<Boolean>) object in the Cache instance
     */
    static class JobId {

        private final String deploymentId;
        private final JobType type;

        public JobId(String deploymentId, JobType type) {
            this.deploymentId = deploymentId;
            this.type = type;
        }

        public boolean matches(String deploymentId) {
            if (deploymentId == null) {
                return false;
            }
            return this.deploymentId.equals(deploymentId);
        }

        public boolean matches(JobType type) {
            if (type == null) {
                return false;
            }
            return this.type.equals(type);
        }

        // only equal to itself
        public boolean equals(Object obj) {
            return (this == obj);
        }

        public String toString() {
            return this.deploymentId + "/" + this.type;
        }
    }

    /**
     * Class used for jobs submitted to the {@link Executor} instance.
     */
    private static class DeploymentJobCallable implements Callable<Boolean> {

        private KModuleDeploymentUnit deploymentUnit;
        private JobType type;
        private KModuleDeploymentService deploymentService;

        public DeploymentJobCallable(KModuleDeploymentUnit depUnit, JobType type, KModuleDeploymentService deploymentService) {
            this.deploymentUnit = depUnit;
            this.type = type;
            this.deploymentService = deploymentService;
        }

        private void makeGarbageCollectionEasy() {
            this.type = null;
            this.deploymentService = null;
            this.deploymentUnit = null;
        }

        @Override
        public Boolean call() throws Exception {
            boolean success = false;
            switch (type) {
            case DEPLOY:
                try {
                    deploymentService.deploy(deploymentUnit);
                    logger.debug("Deployment unit [" + deploymentUnit.getIdentifier() + "] deployed");
                    success = true;
                } catch (Exception e) {
                    logger.error("Unable to deploy [" + deploymentUnit.getIdentifier() + "]", e);
                }
                break;
            case UNDEPLOY:
                try {
                    deploymentService.undeploy(deploymentUnit);
                    logger.debug("Deployment unit [" + deploymentUnit.getIdentifier() + "] undeployed");
                    success = true;
                } catch (Exception e) {
                    logger.error("Unable to undeploy [" + deploymentUnit.getIdentifier() + "]", e);
                }
                break;
            default:
                logger.error("Unknown " + JobType.class.getSimpleName() + " type (" + type.toString() + "), not taking any action");
            }

            makeGarbageCollectionEasy();
            return success;
        }
    }

    int getMaxJobQueueSize() {
        return maxQueueSize;
    }
}
