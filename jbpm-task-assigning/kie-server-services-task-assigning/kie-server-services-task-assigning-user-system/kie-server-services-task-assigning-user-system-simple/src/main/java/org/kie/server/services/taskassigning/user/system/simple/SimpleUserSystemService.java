/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.user.system.simple;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.server.services.taskassigning.user.system.api.Group;
import org.kie.server.services.taskassigning.user.system.api.User;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SimpleUserSystemService implements UserSystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUserSystemService.class);

    protected static final String USERS_FILE = "org.kie.server.services.taskassigning.user.system.simple.users";
    protected static final String AFFINITIES_FILE = "org.kie.server.services.taskassigning.user.system.simple.affinities";
    protected static final String SKILLS_FILE = "org.kie.server.services.taskassigning.user.system.simple.skills";

    protected WildflyUtil.UserGroupInfo userGroupInfo = new WildflyUtil.UserGroupInfo(new ArrayList<>(), new ArrayList<>());
    protected Map<String, User> userById = new HashMap<>();
    protected Exception error = null;

    private static final String NAME = "SimpleUserSystemService";

    public SimpleUserSystemService() {
        //SPI constructor
    }

    @Override
    public void start() {
        final String usersFile = System.getProperty(USERS_FILE);
        try {
            if (isEmpty(usersFile)) {
                LOGGER.warn("No users file configuration was provided. Please configure the property:" + USERS_FILE);
                return;
            }
            this.userGroupInfo = WildflyUtil.buildInfo(URI.create(usersFile));
            this.userById = userGroupInfo.getUsers().stream().collect(Collectors.toMap(User::getId,
                                                                                       Function.identity()));
        } catch (Exception e) {
            LOGGER.error("An error was produced during users file loading from file: " + usersFile, e);
            error = e;
            userGroupInfo = new WildflyUtil.UserGroupInfo(new ArrayList<>(), new ArrayList<>());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<User> findAllUsers() {
        return userGroupInfo.getUsers();
    }

    @Override
    public List<Group> findAllGroups() {
        return userGroupInfo.getGroups();
    }

    @Override
    public void test() throws Exception {
        if (error != null) {
            throw error;
        }
    }

    @Override
    public User findUser(String id) {
        return userById.get(id);
    }
}
