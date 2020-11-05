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

package org.kie.server.services.jbpm.cluster;

import java.util.Arrays;
import java.util.List;

import org.jbpm.executor.AsynchronousJobEvent;
import org.jbpm.executor.AsynchronousJobListener;
import org.jbpm.executor.RequeueAware;
import org.kie.api.cluster.ClusterAwareService;
import org.kie.api.cluster.ClusterListener;
import org.kie.api.cluster.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusteredJobFailOverListener implements ClusterListener, AsynchronousJobListener{

    private static final Logger logger = LoggerFactory.getLogger(ClusteredJobFailOverListener.class);

    private ClusterAwareService clusterService;
    private RequeueAware executorService;
    
    public ClusteredJobFailOverListener(ClusterAwareService clusterService, RequeueAware executorService) {
        this.clusterService = clusterService;
        this.executorService = executorService;
    }

    @Override
    public void nodeJoined(ClusterNode node) {
        logger.info("Node joined in cluster {} node {}", node.getServerId(), node);
    }

    @Override
    public void nodeLeft(ClusterNode node) {
        // all the jobs belonging to the partition need to be requeued
        List<Long> jobs = clusterService.getDataFromPartition(ClusterAwareService.CLUSTER_JOBS_KEY, node.toKey());
        if(jobs == null || jobs.isEmpty()) {
            return;
        }

        logger.info("Node left cluster {}, failing over and requeuing {}", node, Arrays.toString(jobs.toArray()));
        jobs.forEach(jobId -> {
            try {
                executorService.requeueById(jobId);
            } catch ( IllegalArgumentException e) {
                logger.warn("Job was already completed or cancelled {}. Cannot be rescheduled", jobId);
            }
        });
    }

    @Override
    public void afterJobScheduled(AsynchronousJobEvent event) {
        logger.debug("Adding job scheduled {} for failover", event);
        Long data = event.getJob().getId();
        clusterService.addData(ClusterAwareService.CLUSTER_JOBS_KEY, clusterService.getThisNode().toKey(), data);
    }


    @Override
    public void afterJobExecuted(AsynchronousJobEvent event) {
        logger.debug("Removing executed job {} from failover", event.getJob());
        Long data = event.getJob().getId();
        clusterService.removeData(ClusterAwareService.CLUSTER_JOBS_KEY, clusterService.getThisNode().toKey(), data);
    }

    @Override
    public void afterJobCancelled(AsynchronousJobEvent event) {
        logger.debug("Removing cancelled job {} from failover", event.getJob());
        Long data = event.getJob().getId();
        clusterService.removeData(ClusterAwareService.CLUSTER_JOBS_KEY, clusterService.getThisNode().toKey(), data);
    }

    
    @Override
    public void beforeJobScheduled(AsynchronousJobEvent event) {
        // do nothing
    }

    @Override
    public void beforeJobExecuted(AsynchronousJobEvent event) {
        // do nothing
    }

    @Override
    public void beforeJobCancelled(AsynchronousJobEvent event) {
        // do nothing
    }


}
