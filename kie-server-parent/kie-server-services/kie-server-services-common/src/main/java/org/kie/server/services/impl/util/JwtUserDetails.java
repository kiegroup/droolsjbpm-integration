/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.impl.util;

import java.util.ArrayList;
import java.util.List;

public class JwtUserDetails {
    String user;
    List<String> roles;

    public JwtUserDetails() {
        this.user = null;
        this.roles = new ArrayList<>();
    }

    public JwtUserDetails(String user, List<String> roles) {
        this.user = user;
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getUser() {
        return user;
    }

    public boolean isLogged() {
        return user != null;
    }

}