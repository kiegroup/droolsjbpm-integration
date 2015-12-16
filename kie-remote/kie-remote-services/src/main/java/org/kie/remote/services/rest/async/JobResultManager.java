/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.rest.async;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.runtime.query.QueryContext;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
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

    private int maxCacheSize = 10000;

    @Inject
    private Instance<ExecutorService> jobExecutor;

    /**
     * Initialization method to initialize the 2 caches that hold the job result information.
     *
     */
    @PostConstruct
    public void start() {
        if (!created.compareAndSet(0, 1)) {
            throw new KieRemoteServicesInternalError("Only 1 JobResultManager instance is allowed per container!");
        }
        Cache<JaxbDeploymentJobResult> cache = new Cache<JaxbDeploymentJobResult>(maxCacheSize);
        jobs = Collections.synchronizedMap(cache);
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
    }

    /**
     * Get a job using the job's unique id
     * @param jobId The job id
     * @return The {@link JaxbDeploymentJobResult} instance
     */
    public JaxbDeploymentJobResult getJob(String jobId) {
        logger.debug( "Getting job [{}]");
        JaxbDeploymentJobResult job = jobs.get(jobId);

        if (job != null && !JaxbDeploymentStatus.ACCEPTED.equals(job.getDeploymentUnit().getStatus())) {
            return job;
        }
        if (!jobExecutor.isUnsatisfied()) {

            List<RequestInfo> jobsFound = jobExecutor.get().getRequestsByBusinessKey(jobId, new QueryContext());

            if (jobsFound != null && !jobsFound.isEmpty()) {
                RequestInfo executorJob = jobsFound.get(0);
                JaxbDeploymentJobResult jobFromRequest = (JaxbDeploymentJobResult) getItemFromRequestOutput("JobResult", executorJob);
                if (jobFromRequest == null) {
                    jobFromRequest = (JaxbDeploymentJobResult) getItemFromRequestInput("jobResult", executorJob);
                }

                if (jobFromRequest != null) {
                    job = jobFromRequest;
                    jobs.put(jobId, job);
                }
            }
        }

        return job;
    }

    protected Object getItemFromRequestInput(String itemName, RequestInfo requestInfo) {
        CommandContext ctx = null;
        byte[] requestData = requestInfo.getRequestData();
        if (requestData != null) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new ByteArrayInputStream(requestData));
                ctx = (CommandContext) in.readObject();
            } catch (Exception e) {
                logger.debug("Exception while deserializing context data of job with id {}", requestInfo.getId(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {

                    }
                }
            }
        }

        if (ctx != null && ctx.getData(itemName) != null) {
            return ctx.getData(itemName);
        }

        return null;
    }

    protected Object getItemFromRequestOutput(String itemName, RequestInfo requestInfo) {
        ExecutionResults execResults = null;
        byte[] responseData = requestInfo.getResponseData();
        if (responseData != null) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new ByteArrayInputStream(responseData));
                execResults = (ExecutionResults) in.readObject();
            } catch (Exception e) {
                logger.debug("Exception while deserializing context data of job with id {}", requestInfo.getId(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {

                    }
                }
            }
        }

        if (execResults != null && execResults.getData(itemName) != null) {
            return execResults.getData(itemName);
        }

        return null;
    }
}
