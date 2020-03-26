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

package org.kie.server.services.taskassigning.user.system.simple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.services.taskassigning.user.system.api.Group;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService.USERS_FILE_NOT_CONFIGURED_ERROR;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService.USERS_INFO_LOADING_ERROR;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.AFFINITIES_FILE;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.GROUP1;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.GROUP2;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.GROUP3;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.RESOURCES;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.SKILLS_FILE;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.USER1;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.USER2;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.USER3;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.USER4;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.USER5;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelperTest.USERS_FILE;

public class SimpleUserSystemServiceTest {

    private static final String NON_EXISTING_PATH = "a_path_that_dont_exists";

    private SimpleUserSystemService userSystem;
    private static final List<String> USERS = Arrays.asList(USER1, USER2, USER3, USER4, USER5);
    private static final List<String> GROUPS = Arrays.asList(GROUP1, GROUP2, GROUP3);

    private String usersPath;
    private String skillsPath;
    private String affinitiesPath;

    @Before
    public void setUp() throws Exception {
        usersPath = SimpleUserSystemServiceHelperTest.getTestFile(RESOURCES, USERS_FILE).toAbsolutePath().toString();
        skillsPath = SimpleUserSystemServiceHelperTest.getTestFile(RESOURCES, SKILLS_FILE).toAbsolutePath().toString();
        affinitiesPath = SimpleUserSystemServiceHelperTest.getTestFile(RESOURCES, AFFINITIES_FILE).toAbsolutePath().toString();
        System.setProperty(SimpleUserSystemService.USERS_FILE, usersPath);
        System.setProperty(SimpleUserSystemService.SKILLS_FILE, skillsPath);
        System.setProperty(SimpleUserSystemService.AFFINITIES_FILE, affinitiesPath);
        userSystem = new SimpleUserSystemService();
    }

    @After
    public void cleanUp() {
        System.clearProperty(SimpleUserSystemService.USERS_FILE);
        System.clearProperty(SimpleUserSystemService.SKILLS_FILE);
        System.clearProperty(SimpleUserSystemService.AFFINITIES_FILE);
    }

    @Test
    public void startSuccess() {
        Assertions.assertThatCode(() -> userSystem.start())
                .doesNotThrowAnyException();
    }

    @Test
    public void startWithoutSkillsAndAffinitiesSuccess() {
        System.clearProperty(SimpleUserSystemService.SKILLS_FILE);
        System.clearProperty(SimpleUserSystemService.AFFINITIES_FILE);
        Assertions.assertThatCode(() -> userSystem.start())
                .doesNotThrowAnyException();
    }

    @Test
    public void startWithUsersFileNotConfiguredError() {
        System.clearProperty(SimpleUserSystemService.USERS_FILE);
        String expectedMessage = String.format(USERS_FILE_NOT_CONFIGURED_ERROR, SimpleUserSystemService.USERS_FILE);
        Assertions.assertThatThrownBy(() -> userSystem.start())
                .hasMessage(expectedMessage);
    }

    @Test
    public void startWithUsersFileLoadingError() {
        System.setProperty(SimpleUserSystemService.USERS_FILE, NON_EXISTING_PATH);
        String expectedMessage = String.format(USERS_INFO_LOADING_ERROR, NON_EXISTING_PATH, skillsPath, affinitiesPath);
        Assertions.assertThatThrownBy(() -> userSystem.start())
                .hasMessage(expectedMessage);
    }

    @Test
    public void startWithSkillsFileLoadingError() {
        System.setProperty(SimpleUserSystemService.SKILLS_FILE, NON_EXISTING_PATH);
        String expectedMessage = String.format(USERS_INFO_LOADING_ERROR, usersPath, NON_EXISTING_PATH, affinitiesPath);
        Assertions.assertThatThrownBy(() -> userSystem.start())
                .hasMessage(expectedMessage);
    }

    @Test
    public void startWithAffinitiesFileLoadingError() {
        System.setProperty(SimpleUserSystemService.AFFINITIES_FILE, NON_EXISTING_PATH);
        String expectedMessage = String.format(USERS_INFO_LOADING_ERROR, usersPath, skillsPath, NON_EXISTING_PATH);
        Assertions.assertThatThrownBy(() -> userSystem.start())
                .hasMessage(expectedMessage);
    }

    @Test
    public void getName() {
        assertEquals(SimpleUserSystemService.NAME, userSystem.getName());
    }

    @Test
    public void test() {
        userSystem.test();
    }

    @Test
    public void findUser() {
        userSystem.start();
        for (String user : USERS) {
            assertNotNull("User: " + user + " was not found", userSystem.findUser(user));
        }
    }

    @Test
    public void findAllUsers() {
        userSystem.start();
        List<String> userIds = userSystem.findAllUsers().stream()
                .map(User::getId)
                .collect(Collectors.toList());

        for (String user : USERS) {
            assertTrue("User: " + user + " was not found", userIds.contains(user));
        }
    }

    @Test
    public void findAllGroups() {
        userSystem.start();
        List<String> groupIds = userSystem.findAllGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        for (String user : GROUPS) {
            assertTrue("Group: " + user + " was not found", groupIds.contains(user));
        }
    }
}
