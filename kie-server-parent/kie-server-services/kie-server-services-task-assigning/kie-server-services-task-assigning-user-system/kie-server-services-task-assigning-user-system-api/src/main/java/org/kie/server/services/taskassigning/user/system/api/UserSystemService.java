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

package org.kie.server.services.taskassigning.user.system.api;

import java.util.List;

public interface UserSystemService {

    /**
     * Invoked by the task assigning integration as part of the initialization procedure and before any other method
     * is invoked.
     */
    void start();

    /**
     * Invoked by the task assigning integration as part of the initialization procedure and after the start() method
     * is invoked.
     * @throws Exception if the test method failed.
     */
    void test() throws Exception;

    /***
     * @return the name of the UserSystemService implementation.
     */
    String getName();

    /**
     * @return the list of all users present in the external user system. This method is normally invoked each time
     * the solver is initialized or when the users information is updated from the external user system.
     */
    List<User> findAllUsers();

    /**
     * Get the user information for a particular user.
     * @param id user identifier for querying.
     * @return the User corresponding to the given identifier, null if no user was found.
     */
    User findUser(String id);
}
