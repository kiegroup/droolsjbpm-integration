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

package org.jbpm.task.assigning.model;

import java.util.Objects;

public class TypedLabel {

    enum Type {
        SKILL,
        AFFINITY
    }

    private Type type;
    private String value;

    private TypedLabel(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static TypedLabel newSkill(String value) {
        return new TypedLabel(Type.SKILL, value);
    }

    public static TypedLabel newAffinity(String value) {
        return new TypedLabel(Type.SKILL, value);
    }

    public String getValue() {
        return value;
    }

    public boolean isSkill() {
        return Type.SKILL == type;
    }

    public boolean isAffinity() {
        return Type.AFFINITY == type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypedLabel)) {
            return false;
        }
        TypedLabel that = (TypedLabel) o;
        return type == that.type &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
