/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.hacep.core.infra.election;

import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.Test;
import org.kie.hacep.core.CoreKube;
import org.kie.hacep.core.infra.election.LeaderElection;
import org.kie.hacep.core.infra.election.LeaderElectionImpl;

import static org.junit.Assert.*;

public class CoreKubeTest {

    @Test
    public void leaderElectionTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        election.stop();
    }

    @Test
    public void lookupNewLeaderInfoTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.lookupNewLeaderInfo();
        election.stop();
    }

    @Test(expected = KubernetesClientException.class)
    public void pullClusterMembersTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.pullClusterMembers();
        election.stop();
    }

    @Test(expected = KubernetesClientException.class)
    public void pullConfigMapTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.pullConfigMap();
        election.stop();
    }

    @Test
    public void refreshStatusTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.refreshStatus();;
        election.stop();
    }

    @Test
    public void refreshStatusBecomingLeaderTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.refreshStatusBecomingLeader();;
        election.stop();
    }

    @Test
    public void refreshStatusLeaderTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.refreshStatusLeader();;
        election.stop();
    }

    @Test
    public void refreshStatusNotLeaderTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.refreshStatusNotLeader();;
        election.stop();
    }

    @Test
    public void rescheduleAfterDelayTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.rescheduleAfterDelay();;
        election.stop();
    }

    @Test
    public void tryAcquireLeadershipTest() {
        CoreKube kube = new CoreKube("default", null);
        LeaderElection election =kube.getLeaderElection();
        assertNotNull(election);
        election.start();
        LeaderElectionImpl impl = (LeaderElectionImpl) election;
        impl.tryAcquireLeadership();
        election.stop();
    }

}
