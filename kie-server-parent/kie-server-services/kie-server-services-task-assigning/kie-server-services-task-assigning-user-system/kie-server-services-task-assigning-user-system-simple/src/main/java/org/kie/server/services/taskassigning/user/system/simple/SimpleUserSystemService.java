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

import java.nio.file.Paths;
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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class SimpleUserSystemService implements UserSystemService {

    static final String USERS_FILE_NOT_CONFIGURED_ERROR = "No users file configuration was provided. Please configure the property: %s";

    static final String USERS_INFO_LOADING_ERROR = "An error was produced during users information loading from files, users: %s, skills: %s, affinities: %s";

    /**
     * Property name for configuring the path to the users definition file.
     */
    static final String USERS_FILE = "org.kie.server.services.taskassigning.user.system.simple.users";

    /**
     * Property name for configuring the path to the optional users skills definition file.
     */
    static final String SKILLS_FILE = "org.kie.server.services.taskassigning.user.system.simple.skills";

    /**
     * Property name for configuring the path to the optional users affinities definition file.
     */
    static final String AFFINITIES_FILE = "org.kie.server.services.taskassigning.user.system.simple.affinities";

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUserSystemService.class);

    private SimpleUserSystemServiceHelper.UserGroupInfo userGroupInfo = new SimpleUserSystemServiceHelper.UserGroupInfo(new ArrayList<>(), new ArrayList<>());
    private Map<String, User> userById = new HashMap<>();
    protected Exception error = null;

    public static final String NAME = "SimpleUserSystemService";

    public SimpleUserSystemService() {
        //SPI constructor
    }

    @Override
    public void start() {
        final String usersFile = System.getProperty(USERS_FILE);
        if (isEmpty(usersFile)) {
            String msg = String.format(USERS_FILE_NOT_CONFIGURED_ERROR, USERS_FILE);
            throw new SimpleUserSystemServiceException(msg);
        }
        final String skillsFile = System.getProperty(SKILLS_FILE);
        if (isNotEmpty(skillsFile)) {
            LOGGER.info("Using skills configuration from: {}", skillsFile);
        }
        final String affinitiesFile = System.getProperty(AFFINITIES_FILE);
        if (isNotEmpty(affinitiesFile)) {
            LOGGER.info("Using affinities configuration from: {}", affinitiesFile);
        }

        try {
            this.userGroupInfo = SimpleUserSystemServiceHelper.buildInfo(Paths.get(usersFile),
                                                                         isNotEmpty(skillsFile) ? Paths.get(skillsFile) : null,
                                                                         isNotEmpty(affinitiesFile) ? Paths.get(affinitiesFile) : null);
            this.userById = userGroupInfo.getUsers().stream().collect(Collectors.toMap(User::getId, Function.identity()));
        } catch (Exception e) {
            String msg = String.format(USERS_INFO_LOADING_ERROR, usersFile, skillsFile, affinitiesFile);
            throw new SimpleUserSystemServiceException(msg, e);
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
    public void test() {
        // no-op for this implementation.
    }

    @Override
    public User findUser(String id) {
        return userById.get(id);
    }

    public class SimpleUserSystemServiceException extends RuntimeException {

        public SimpleUserSystemServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public SimpleUserSystemServiceException(String message) {
            super(message);
        }
    }
}
