package org.drools;

import java.io.Serializable;

import org.drools.rule.LiteralConstraint;

public class RuleBaseInfo
    implements
    Serializable {
    private String id;
    private String name;

    public RuleBaseInfo() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the uuid
     */
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(final Object object) {
        if ( this == object ) {
            return true;
        }

        if ( object == null || object.getClass() != RuleBaseInfo.class ) {
            return false;
        }

        final RuleBaseInfo other = (RuleBaseInfo) object;

        return this.id.equals( other.id ) && this.name.equals( other.name );
    }
}
