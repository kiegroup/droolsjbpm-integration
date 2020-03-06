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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.fabric8.kubernetes.api.model.ConfigMap;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class ConfigMapLockUtilsTest {

    static Logger logger = LoggerFactory.getLogger(ConfigMapLockUtilsTest.class);

    @Test
    public void methodsTest(){
        String groupName = "drools-group";
        String leader = "leader-x13X";
        Date timestamp = Calendar.getInstance().getTime();
        Set<String> members = new HashSet<>(Arrays.asList("Qui", "Quo", "Qua"));
        LeaderInfo info = new LeaderInfo(groupName, leader, timestamp, members);
        logger.info("leaderInfo:{}", info.toString());
        assertFalse(info.hasEmptyLeader());
        assertFalse(info.isValidLeader(null));
        assertFalse(info.hasValidLeader());
        assertEquals(groupName,info.getGroupName());
        assertEquals(leader,info.getLeader());
        assertEquals(timestamp,info.getLocalTimestamp());
        assertEquals(members,info.getMembers());
        ConfigMap configMap = ConfigMapLockUtils.createNewConfigMap("my-map", info);
        assertNotNull(configMap);
        LeaderInfo leaderInfo = ConfigMapLockUtils.getLeaderInfo(configMap, members, groupName);
        logger.info("leaderInfo:{}", leaderInfo.toString());
        assertNotNull(leaderInfo);
        ConfigMap newConfigMap = ConfigMapLockUtils.getConfigMapWithNewLeader(configMap, leaderInfo);
        assertNotNull(newConfigMap);
    }
}
