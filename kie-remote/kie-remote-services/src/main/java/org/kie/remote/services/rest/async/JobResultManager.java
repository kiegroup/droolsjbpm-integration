package org.kie.remote.services.rest.async;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.kie.remote.services.rest.async.cmd.JobType;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class caches the job result information 
 * in order to provide it upon request (so that users
 * can check the status of a deployment or undeployment job). 
 */
@ApplicationScoped
public class JobResultManager {

    private static final Logger logger = LoggerFactory.getLogger(JobResultManager.class);
    private static AtomicInteger created = new AtomicInteger(0);

    private static class Cache<T> extends LinkedHashMap<String, T> {

        /** generated serial version UID */
        private static final long serialVersionUID = -5369827812060944667L;
        
        private int maxSize = 1000;

        public Cache(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, T> stringFutureEntry) {
            return size() > maxSize;
        }
    }

    private Map<String, JaxbDeploymentJobResult> jobs = null;
    private Map<String, String> deploymentIdMostRecentJobIdMap = null;

    private int maxCacheSize = 10000;

    /**
     * Initialization method to initialize the 2 caches that hold the job result information.
     *
     */
    @PostConstruct
    public void start() {
        if (!created.compareAndSet(0, 1)) {
            throw new IllegalStateException("Only 1 JobResultManager instance is allowed per container!");
        }
        Cache<JaxbDeploymentJobResult> cache = new Cache<JaxbDeploymentJobResult>(maxCacheSize);
        jobs = Collections.synchronizedMap(cache);
        Cache<String> idCache = new Cache<String>(maxCacheSize);
        deploymentIdMostRecentJobIdMap = Collections.synchronizedMap(idCache);
    }

    /**
     * Add a job to the cache
     * @param jobId The unique id of the job (unique to the kie-services-remote code)
     * @param job The job to cache
     * @param jobType The job type, for logging purposes
     */
    public void putJob(String jobId, JaxbDeploymentJobResult job, JobType jobType) {
        logger.debug( "Adding job [{}] to cache");
        jobs.put(jobId, job);
       
        String deploymentId = job.getDeploymentUnit().getIdentifier();
        logger.debug( "Adding job id [{}] to \"most recent job\" cache");
        String oldJobId = deploymentIdMostRecentJobIdMap.put(deploymentId, jobId);
        if( oldJobId != null ) { 
            JaxbDeploymentJobResult oldJobResult = jobs.get(oldJobId);
            if( ! JaxbDeploymentStatus.DEPLOYED.equals(oldJobResult.getDeploymentUnit().getStatus()) 
                    && ! JaxbDeploymentStatus.UNDEPLOYED.equals(oldJobResult.getDeploymentUnit().getStatus()) )
            logger.info( "New {} job [{}] for '{}' requested while old job [{}] has status {}",
                    jobType.toString().toLowerCase(), 
                    jobId, 
                    oldJobResult.getDeploymentUnit().getIdentifier(),
                    oldJobId, 
                    oldJobResult.getDeploymentUnit().getStatus());
        }
    }

    /**
     * Get a job using the job's unique id
     * @param jobId The job id
     * @return The {@link JaxbDeploymentJobResult} instance
     */
    public JaxbDeploymentJobResult getJob(String jobId) {
        logger.debug( "Getting job [{}]");
       return jobs.get(jobId); 
    }
   
    /**
     * Get the most recent job requested for a given deployment
     * @param deploymentId The id of the deployment
     * @return The {@link JaxbDeploymentJobResult} with the job information
     */
    public JaxbDeploymentJobResult getMostRecentJob(String deploymentId) {
        logger.debug( "Getting most recent job for '{}'", deploymentId);
        String jobId = deploymentIdMostRecentJobIdMap.get(deploymentId);
        if( jobId != null ) { 
            return jobs.get(jobId);
        }
        return null;
    }

}
