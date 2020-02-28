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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.kie.server.services.taskassigning.user.system.api.Group;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class WildflyUtil {

    private WildflyUtil() {
    }

    public static UserGroupInfo buildInfo(URI uri) throws IOException {
        try (InputStream in = Files.newInputStream(Paths.get(uri))) {
            return buildInfo(in);
        }
    }

    /**
     * Reads a Wildfly roles.properties configuration format file and extracts the user definitions and the corresponding
     * groups.
     * @param input InputStream with the user + roles file in the WF format.
     * @return a UserGroupInfo instance with the Users and Groups loaded.
     * @throws IOException if an I/O error occurs
     */
    public static UserGroupInfo buildInfo(InputStream input) throws IOException {
        final List<User> users = new ArrayList<>();
        final List<Group> groups = new ArrayList<>();
        final Map<String, Group> groupMap = new HashMap<>();

        final List<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toList());
        for (String line : lines) {
            String[] lineSplit = line.split("=");
            if (lineSplit.length > 0) {
                String userLogin = lineSplit[0].trim();
                if (isNotEmpty(userLogin)) {
                    Set<Group> userGroups = new HashSet<>();
                    if (lineSplit.length > 1) {
                        String encodedGroups = lineSplit[1].trim();
                        String[] userGroupsSplit = encodedGroups.split(",");
                        for (String groupNameRaw : userGroupsSplit) {
                            String groupName = groupNameRaw.trim();
                            if (isNotEmpty(groupName)) {
                                Group group = groupMap.computeIfAbsent(groupName, GroupImpl::new);
                                if (!groups.contains(group)) {
                                    groups.add(group);
                                }
                                userGroups.add(group);
                            }
                        }
                    }
                    User user = new UserImpl(userLogin, userGroups);
                    users.add(user);
                }
            }
        }
        return new UserGroupInfo(users, groups);
    }

    public static class UserGroupInfo {

        private List<User> users;
        private List<Group> groups;

        UserGroupInfo(List<User> users, List<Group> groups) {
            this.users = users;
            this.groups = groups;
        }

        public List<User> getUsers() {
            return users;
        }

        public List<Group> getGroups() {
            return groups;
        }
    }
}
