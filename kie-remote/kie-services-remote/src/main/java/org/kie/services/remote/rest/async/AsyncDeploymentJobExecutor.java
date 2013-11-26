package org.kie.services.remote.rest.async;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.resteasy.logging.Logger;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;

import static org.kie.services.remote.rest.DeploymentResource.*;

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
 * In order to satisfy that last requirement (no concurrent un/deployment of the same deployment unit), we use a single threaded 
 * exector, which queues up other jobs.
 */
@ApplicationScoped
public class AsyncDeploymentJobExecutor {

    private final static Logger logger = Logger.getLogger(AsyncDeploymentJobExecutor.class);

    protected final ExecutorService executor;
    private final Map<String, Future<Boolean>> jobs;

    private int maxCacheSize = 100;

    private static enum JobType {
        DEPLOY, UNDEPLOY;
    }

    public AsyncDeploymentJobExecutor() {
        Cache<Boolean> cache = new Cache<Boolean>(maxCacheSize);
        jobs = Collections.synchronizedMap(cache);
        executor = Executors.newSingleThreadExecutor();
    }

    public JaxbDeploymentJobResult submitDeployJob(KModuleDeploymentService deploymentService, KModuleDeploymentUnit depUnit) {
        return submitJob(deploymentService, depUnit, JobType.DEPLOY);
    }

    public JaxbDeploymentJobResult submitUndeployJob(KModuleDeploymentService deploymentService, KModuleDeploymentUnit depUnit) {
        return submitJob(deploymentService, depUnit, JobType.UNDEPLOY);
    }

    private JaxbDeploymentJobResult submitJob(KModuleDeploymentService deploymentService, KModuleDeploymentUnit depUnit,
            JobType type) {
        if (jobs.size() > maxCacheSize + 1) {
            String msg = "Waiting for existing un/deploy jobs to complete first.";
            logger.info(type.toString() + " job NOT submitted: " + msg);
            return new JaxbDeploymentJobResult(msg, false, convertKModuleDepUnitToJaxbDepUnit(depUnit), type.toString());
        }

        // setup
        String jobId = depUnit.getIdentifier() + "-" + type.toString();

        /**
         * If there's already a job for the same deployment in the queue,
         * don't do anything.
         */
        Future<Boolean> previousJob = jobs.get(jobId);
        if (previousJob != null) {
            if (!previousJob.isDone()) {
                String msg = "A job already exists to " + type.toString().toLowerCase() + " this deployment.";
                logger.info(type.toString() + " job NOT submitted: " + msg);
                return new JaxbDeploymentJobResult(msg, false, convertKModuleDepUnitToJaxbDepUnit(depUnit), type.toString());
            } else {
                jobs.values().remove(previousJob);
            }
        }

        // submit job
        DeploymentJobCallable job = new DeploymentJobCallable(depUnit, type, deploymentService);
        Future<Boolean> newJob = executor.submit(job);
        previousJob = jobs.put(jobId, newJob);

        StringBuilder statusMsg = new StringBuilder("Deployment (" + type.toString().toLowerCase() + ") job submitted.");
        /**
         * If another request has managed to also submit a job,
         * then we try to cancel it (before it runs, no interrupting!)
         * (this job then runs in it's place, obviously)
         */
        if (previousJob != null && !previousJob.isDone()) {
            if (previousJob.cancel(false)) {
                String msg = "A previous identical job has been cancelled and this job will take its place.";
                logger.info(type.toString() + " job submitted: " + msg);
                statusMsg.append(" ").append(msg);
            } else {
                String msg = "Unable to cancel a previous identical job. This job may fail.";
                logger.info(type.toString() + " job submitted: " + msg);
                statusMsg.append(" ").append(msg);
            }
        } else {
            logger.info(type.toString() + " job submitted succesfully");
        }

        return new JaxbDeploymentJobResult(statusMsg.toString(), true, convertKModuleDepUnitToJaxbDepUnit(depUnit), type.toString());
    }

    public JaxbDeploymentStatus getStatus(String deploymentUnitId) {
        Future<Boolean> deployJob = jobs.get(deploymentUnitId + "-" + JobType.DEPLOY);
        Future<Boolean> undeployJob = jobs.get(deploymentUnitId + "-" + JobType.UNDEPLOY);

        if (deployJob == null && undeployJob == null) {
            return JaxbDeploymentStatus.NONEXISTENT;
        } else if (deployJob != null && !deployJob.isDone()) {
            return JaxbDeploymentStatus.DEPLOYING;
        } else if (undeployJob != null && !undeployJob.isDone()) {
            return JaxbDeploymentStatus.UNDEPLOYING;
        } else if (deployJob != null) { // deployJob.isDone()
            jobs.values().remove(deployJob);
            Boolean success = false;
            try { 
                success = deployJob.get(1, TimeUnit.MILLISECONDS);
                if( success == null ) { 
                    throw new KieRemoteServicesInternalError("Impossible error: deployment job did not return a boolean. Contact the developers.");
                }
            } catch (Exception e) {
                // Technically, this should never happen, but if it has, then there's probably something wrong. 
                // It is possible that this is a false negative though.. 
                return JaxbDeploymentStatus.DEPLOY_FAILED;
            }
            if( success ) { 
                return JaxbDeploymentStatus.DEPLOYED;
            } else { 
                return JaxbDeploymentStatus.DEPLOY_FAILED;
            }
        } else { // undeployJob.isDone()
            jobs.values().remove(undeployJob);
            Boolean success = false;
            try { 
                success = undeployJob.get(1, TimeUnit.MILLISECONDS);
                if( success == null ) { 
                    throw new KieRemoteServicesInternalError("Impossible error: deployment job did not return a boolean. Contact the developers.");
                }
            } catch (Exception e) {
                // Technically, this should never happen, but if it has, then there's probably something wrong. 
                // It is possible that this is a false negative though.. 
                return JaxbDeploymentStatus.DEPLOY_FAILED;
            }
            if( success ) { 
                return JaxbDeploymentStatus.UNDEPLOYED;
            } else { 
                return JaxbDeploymentStatus.UNDEPLOY_FAILED;
            }
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
    private static class Cache<T> extends LinkedHashMap<String, Future<T>> {
        private int maxSize = 100;

        public Cache(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Future<T>> stringFutureEntry) {
            return stringFutureEntry.getValue().isDone() && (size() > maxSize);
        }
    }

    /**
     * Class used for jobs submitted to the {@link Executor} instance.
     */
    private static class DeploymentJobCallable implements Callable<Boolean> {

        private final KModuleDeploymentUnit deploymentUnit;
        private final JobType type;
        private final KModuleDeploymentService deploymentService;

        public DeploymentJobCallable(KModuleDeploymentUnit depUnit, JobType type, KModuleDeploymentService deploymentService) {
            this.deploymentUnit = depUnit;
            this.type = type;
            this.deploymentService = deploymentService;
        }

        @Override
        public Boolean call() throws Exception {
            boolean success = false;
            switch (type) {
            case DEPLOY:
                try {
                    deploymentService.deploy(deploymentUnit);
                    logger.debug("Deployment unit '" + deploymentUnit.getIdentifier() + "' deployed.");
                    success = true;
                } catch (Exception e) {
                    logger.error("Unable to deploy '" + deploymentUnit.getIdentifier() + "'", e);
                    success = false;
                }
                break;
            case UNDEPLOY:
                try {
                    deploymentService.undeploy(deploymentUnit);
                    logger.debug("Deployment unit '" + deploymentUnit.getIdentifier() + "' undeployed.");
                    success = true;
                } catch (Exception e) {
                    logger.error("Unable to undeploy '" + deploymentUnit.getIdentifier() + "'", e);
                    success = false;
                }
                break;
            default:
                throw new KieRemoteServicesInternalError("Unknown " + JobType.class.getSimpleName() + " type (" + type.toString()
                        + "), not taking any action.");
            }

            return success;
        }
    }

}
