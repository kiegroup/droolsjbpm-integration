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
package org.kie.server.services.impl.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Class meant to provide utility methods needed to migrate java.security code to JVM 17 and more
 */
public class SecurityMigrationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityMigrationUtils.class);


    /**
     * Helper method to retrieve a list of <b>roles</b> from given <code>Principal</code>s
     * @param principals
     * @return
     */
    public static List<String> getRolesFromPrincipals(Set<Principal> principals) {
        List<String> toReturn = new ArrayList<>();
        for (Principal principal : principals) {
            if (isGroupPrincipal(principal)) {
                Optional<Enumeration<? extends Principal>> optionalGroups = getGroupsEquivalentReflective(principal);
                if (optionalGroups.isPresent()) {
                    Enumeration<? extends Principal> groups = optionalGroups.get();
                    while (groups.hasMoreElements()) {
                        Principal groupPrincipal = groups.nextElement();
                        toReturn.add(groupPrincipal.getName());
                    }
                    break;
                }
            }
        }
        return toReturn;
    }

    /**
     * Helper method used to get the groups equivalent of a given principal using reflection
     * @return an <code>Optional</code> containing the groups equivalent of the given principal or an empty <code>Optional</code> if the principal is not a group
     */
    public static Optional<Enumeration<? extends Principal>> getGroupsEquivalentReflective(Principal principal) {
        try {
            java.lang.reflect.Method membersMethod =
                    principal.getClass().getMethod("members");
            @SuppressWarnings("unchecked")
            Enumeration<? extends Principal> toReturn = (Enumeration<? extends Principal>) membersMethod.invoke(principal);
            return Optional.ofNullable(toReturn);
        } catch (Exception exception) {
            String exceptionMsg = exception.getMessage() != null && !exception.getMessage().isEmpty() ? exception.getMessage() : exception.toString();
            LOGGER.warn("Unable to reflectively get groups for principal' class {} due to {} ", principal.getClass(), exceptionMsg);
            return Optional.empty();
        }
    }

    /**
     * Helper method to verify if a given principal is a <b>group</b>
     * @param p
     * @return
     */
    public static boolean isGroupPrincipal(Principal p) {
        // check for known vendor group principals by class hierarchy name
        Class<?> c = p.getClass();
        while (c != null) {
            for (Class<?> iface : c.getInterfaces()) {
                if ("java.security.acl.Group".equals(iface.getName())) {
                    return true; // vendor class still implements the old interface
                }
            }
            c = c.getSuperclass();
        }
        return p.getClass().getSimpleName().equalsIgnoreCase("SimpleGroup")
                || p.getClass().getSimpleName().equalsIgnoreCase("Group");
    }


}   