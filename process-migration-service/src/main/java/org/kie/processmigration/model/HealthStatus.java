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

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class HealthStatus {

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";

    private String status;

    @JsonInclude(Include.NON_NULL)
    private String message;

    private Instant date = Instant.now();

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public HealthStatus setMessage(String message) {
        this.message = message;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public HealthStatus up() {
        this.status = UP;
        return this;
    }

    public HealthStatus down() {
        this.status = DOWN;
        return this;
    }

    @JsonIgnore
    public boolean isUp() {
        return UP.equals(this.status);
    }

}
