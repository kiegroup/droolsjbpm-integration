/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.testing;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPersistable implements Serializable,
                                                     Comparable<AbstractPersistable> {

    // TODO Use @XmlID @XmlJavaTypeAdapter(IdAdapter.class) to allow CloudProcess's usage of @XmlIDREF
    @PlanningId
    protected Long id;

    protected AbstractPersistable() {
    }

    protected AbstractPersistable(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Used by the GUI to sort the {@link ConstraintMatch} list
     * by {@link ConstraintMatch#getJustificationList()}.
     * @param other never null
     * @return comparison
     */
    public int compareTo(AbstractPersistable other) {
        return new CompareToBuilder()
                .append(getClass().getName(),
                        other.getClass().getName())
                .append(id,
                        other.id)
                .toComparison();
    }

    public String toString() {
        return getClass().getName().replaceAll(".*\\.",
                                               "") + "-" + id;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof AbstractPersistable) {
            AbstractPersistable other = (AbstractPersistable) o;
            return new EqualsBuilder()
                    .append(id,
                            other.id)
                    .isEquals();
        } else {
            return false;
        }
    }
}
