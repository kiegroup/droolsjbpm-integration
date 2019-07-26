/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.model.exceptions;

public class ResourceNotFoundException extends Exception {

    private static final long serialVersionUID = 2319415509210342979L;

    private final String resource;
    private final Long id;

    public ResourceNotFoundException(String resource, Long id) {
        this.resource = resource;
        this.id = id;
    }

    public String getMessage() {
        return String.format("%s not found with id %s", resource, id);
    }
}
