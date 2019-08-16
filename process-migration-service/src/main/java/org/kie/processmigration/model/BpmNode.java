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
package org.kie.processmigration.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class BpmNode {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    private String id;
    private String type;

    public String getName() {
        return name;
    }

    public BpmNode setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public BpmNode setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public BpmNode setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "BpmNode [name=" + name + ", type=" + type
            + ", id=" + id + "]";
    }
}
