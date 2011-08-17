/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.benchmark;

import java.lang.reflect.*;

/**
 * @author Mario Fusco
 */
public class BenchmarkDefinition {

    private Constructor<?> constructor;
    private Object[] args;
    private String description;
    private int repetitions;
    private boolean enabled = true;

    public BenchmarkDefinition(Constructor<?> constructor, Object[] args) {
        this.constructor = constructor;
        this.args = args;
    }

    public String getDescription() {
        return description;
    }
    public BenchmarkDefinition setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getRepetitions() {
        return repetitions;
    }
    public BenchmarkDefinition setRepetitions(int repetitions) {
        this.repetitions = repetitions;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public BenchmarkDefinition setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Benchmark instance() {
        Benchmark benchmark = null;
        try {
            benchmark = (Benchmark)constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return benchmark;
    }
}
