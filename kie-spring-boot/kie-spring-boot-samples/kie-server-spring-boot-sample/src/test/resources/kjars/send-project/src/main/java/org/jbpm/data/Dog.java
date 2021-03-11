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

package org.jbpm.data;

import java.io.Serializable;

import org.kie.api.remote.Remotable;

@Remotable
public class Dog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private double weight;
    
    private String breed;

    public Dog() {
    }

    public double getWeight() {
        return weight;
    }
    
    public String getBreed() {
        return breed;
    }
    
    public Dog(double weight, String breed) {
        this.weight = weight;
        this.breed = breed;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Double.valueOf(weight).hashCode();
        if (breed != null) {
            result = 31 * result + breed.hashCode();
        }
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Dog))
            return false;
        Dog other = (Dog)o;
        boolean breedEquals = (this.breed == null && other.breed == null)
          || (this.breed != null && this.breed.equals(other.breed));
        return this.weight == other.weight && breedEquals;
    }
    
    @Override
    public String toString() {
        return "Dog [weight=" + weight + ", breed=" + breed + "]";
    }

}