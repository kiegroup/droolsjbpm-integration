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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.server.services.taskassigning.user.system.api.Group;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Utility class for loading users definition from a file using the Wildfly roles.properties definition file format.
 * i.e.
 * user1=group1,group2
 * user1=group2,group3
 * <p>
 * and additionally facilitates the reading of user attributes like skills and affinities in a similar format.
 * e.g.
 * skills.properties:
 * user1=skill1,skill2
 * user2=skill3
 * <p>
 * e.g.
 * affinities.properties
 * user1=affinity1,affinity2
 * <p>
 * When configured, the users skills will be loaded in the SKILLS_ATTRIBUTE_NAME. Following the example above
 * user.getAttributes().get(SKILLS_ATTRIBUTE_NAME) will return the String "skill1,skill2");
 * <p>
 * When configured, the users affinities will be loaded in the AFFINITIES_ATTRIBUTE_NAME. Following the example above
 * user1.getAttributes().get(AFFINITIES_ATTRIBUTE_NAME) will return the String "affinity1,affinity2");
 * and
 * user2.getAttributes().get(AFFINITIES_ATTRIBUTE_NAME) will return null.
 */
public class SimpleUserSystemServiceHelper {

    public static final String SKILLS_ATTRIBUTE_NAME = "skills";
    public static final String AFFINITIES_ATTRIBUTE_NAME = "affinities";

    private SimpleUserSystemServiceHelper() {
    }

    /**
     * Reads the users definitions from a file using the Wildfly roles.properties definition file format. See class comments.
     * Additionally loads the users skills and affinities if present.
     * @param users a path to the user + roles file in the WF format.
     * @param skills an optional path to the users skills file.
     * @param affinities an optional path to the users affinities file.
     * @return a UserGroupInfo instance with the Users and Groups loaded.
     * @throws IOException if an I/O error occurs
     */
    public static UserGroupInfo buildInfo(Path users, Path skills, Path affinities) throws IOException {
        try (InputStream usersIn = Files.newInputStream(users);
             InputStream skillsIn = skills != null ? Files.newInputStream(skills) : null;
             InputStream affinitiesIn = affinities != null ? Files.newInputStream(affinities) : null) {
            return buildInfo(usersIn, skillsIn, affinitiesIn);
        }
    }

    /**
     * Reads the users definitions for a file in the Wildfly roles.properties definition file format. See class comments.
     * Additionally loads the users skills and affinities if present.
     * @param usersIn InputStream with the user + roles file in the WF format.
     * @param skillsIn InputStream with the users skills.
     * @param affinitiesIn InputStream with the users affinities.
     * @return a UserGroupInfo instance with the Users and Groups loaded.
     * @throws IOException if an I/O error occurs
     */
    public static UserGroupInfo buildInfo(InputStream usersIn, InputStream skillsIn, InputStream affinitiesIn) throws IOException {

        final Map<String, User> usersMap = new HashMap<>();
        final Map<String, Group> groupMap = new HashMap<>();
        final List<ElementLine> lines = readLines(usersIn);
        final List<ElementLine> skillLines = skillsIn != null ? readLines(skillsIn) : Collections.emptyList();
        final List<ElementLine> affinityLines = affinitiesIn != null ? readLines(affinitiesIn) : Collections.emptyList();

        for (ElementLine line : lines) {
            Set<Group> userGroups = new HashSet<>();
            User user = new UserImpl(line.elementId, userGroups, new HashMap<>());
            line.values.forEach(groupName -> {
                Group group = groupMap.computeIfAbsent(groupName, GroupImpl::new);
                userGroups.add(group);
            });
            usersMap.put(user.getId(), user);
        }
        populateAttribute(usersMap, SKILLS_ATTRIBUTE_NAME, skillLines);
        populateAttribute(usersMap, AFFINITIES_ATTRIBUTE_NAME, affinityLines);
        return new UserGroupInfo(new ArrayList<>(usersMap.values()), new ArrayList<>(groupMap.values()));
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

    private static class ElementLine {

        private String elementId;
        private List<String> values = new ArrayList<>();

        public ElementLine(String elementId) {
            this.elementId = elementId;
        }

        public String getElementId() {
            return elementId;
        }

        public void addValue(String value) {
            values.add(value);
        }

        public List<String> getValues() {
            return values;
        }
    }

    private static void populateAttribute(Map<String, User> usersMap, String attributeName, List<ElementLine> attributeLines) {
        for (ElementLine attributeLine : attributeLines) {
            User user = usersMap.get(attributeLine.getElementId());
            if (user != null) {
                final String value = String.join(",", attributeLine.getValues());
                if (value.length() > 1) {
                    user.getAttributes().put(attributeName, value);
                }
            }
        }
    }

    private static List<ElementLine> readLines(InputStream input) throws IOException {
        final List<String> rawLines = IOUtils.readLines(input, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .filter(line -> !line.startsWith("#"))
                .collect(Collectors.toList());
        final List<ElementLine> lines = new ArrayList<>();
        for (String rawLine : rawLines) {
            final String[] rawLineSplit = rawLine.split("=");
            ElementLine line = readLine(rawLineSplit);
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static ElementLine readLine(String[] rawLineSplit) {
        if (rawLineSplit == null || rawLineSplit.length == 0) {
            return null;
        }
        String elementId = rawLineSplit[0].trim();
        if (isNotEmpty(elementId)) {
            ElementLine line = new ElementLine(elementId);
            if (rawLineSplit.length > 1) {
                String encodedValues = rawLineSplit[1].trim();
                String[] valuesSplit = encodedValues.split(",");
                for (String rawValue : valuesSplit) {
                    String value = rawValue.trim();
                    if (isNotEmpty(value)) {
                        line.addValue(value);
                    }
                }
            }
            return line;
        }
        return null;
    }
}
