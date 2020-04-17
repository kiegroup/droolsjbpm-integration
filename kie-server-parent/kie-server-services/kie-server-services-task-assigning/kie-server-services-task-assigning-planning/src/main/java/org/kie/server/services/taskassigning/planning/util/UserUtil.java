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

package org.kie.server.services.taskassigning.planning.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.kie.server.api.model.taskassigning.UserType;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.planning.data.LabelValueExtractorRegistry;

public class UserUtil {

    private UserUtil() {
    }

    public static User fromExternalUser(org.kie.server.services.taskassigning.user.system.api.User externalUser) {
        final User user = new User(externalUser.getId().hashCode(), externalUser.getId(), true);
        final Set<Group> groups = new HashSet<>();
        user.setGroups(groups);
        if (externalUser.getGroups() != null) {
            externalUser.getGroups().forEach(externalGroup -> groups.add(new Group(externalGroup.getId().hashCode(), externalGroup.getId())));
        }
        user.setAttributes(externalUser.getAttributes());
        LabelValueExtractorRegistry.getInstance()
                .applyLabelValueExtractors(org.kie.server.services.taskassigning.user.system.api.User.class,
                                           externalUser, user::setLabelValues);
        return user;
    }

    public static boolean isUser(String userType) {
        return UserType.USER.equals(userType);
    }

    /**
     * Given a Collection of external users, normally returned by the UserSystemService, produces the filtering of the
     * elements with the following criteria:
     * 1) Null elements are removed from the result.
     * 2) Duplicate users are removed from the result. Since the external users implementation is unknown and the identifier
     * of a user is the "id", two users are considered to be the same if they have the same "id". Meaning that if two users
     * with the same "id" are encountered only one instance will be included in the result, typically the first instance
     * found.
     * @param externalUsers a collection with the external users to filter.
     * @return a Stream with the filtered users.
     */
    public static Stream<org.kie.server.services.taskassigning.user.system.api.User> filterDuplicates(Collection<org.kie.server.services.taskassigning.user.system.api.User> externalUsers) {
        if (externalUsers == null) {
            return Stream.empty();
        } else {
            final Set<String> included = new HashSet<>();
            return externalUsers.stream()
                    .filter(Objects::nonNull)
                    .filter(externalUser -> {
                        if (!included.contains(externalUser.getId())) {
                            included.add(externalUser.getId());
                            return true;
                        }
                        return false;
                    });
        }
    }
}
