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

package org.kie.integration.tomcat;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class SimpleRolePrincipal extends SimplePrincipal implements Group {

    private Set<Principal> principals = new HashSet<Principal>();

    public SimpleRolePrincipal(String[] roles) {
        super("Roles");
        for (String role : roles ) {
            principals.add(new SimplePrincipal(role));
        }
    }


    @Override
    public boolean addMember(Principal principal) {
        return principals.add(principal);
    }

    @Override
    public boolean removeMember(Principal principal) {
        return principals.remove(principal);
    }

    @Override
    public boolean isMember(Principal principal) {
        return principals.contains(principal);
    }

    @Override
    public Enumeration<? extends Principal> members() {
        return Collections.enumeration(principals);
    }


}
