/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class Box implements Serializable {

    static final long serialVersionUID = 1L;

    private BigInteger id;
    private List<Integer> numbers;
    private String name;
    private Boolean valid;

    public Box() {
    }

    public BigInteger getId() {
        return this.id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public List<Integer> getNumbers() {
        return this.numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Box(BigInteger id,
            List<Integer> numbers, String name,
            Boolean valid) {
        this.id = id;
        this.numbers = numbers;
        this.name = name;
        this.valid = valid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((numbers == null) ? 0 : numbers.hashCode());
        result = prime * result + ((valid == null) ? 0 : valid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Box other = (Box) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (numbers == null) {
            if (other.numbers != null)
                return false;
        } else if (!numbers.equals(other.numbers))
            return false;
        if (valid == null) {
            if (other.valid != null)
                return false;
        } else if (!valid.equals(other.valid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Box [id=" + id + ", numbers=" + numbers + ", name=" + name + ", valid=" + valid + "]";
    }

}