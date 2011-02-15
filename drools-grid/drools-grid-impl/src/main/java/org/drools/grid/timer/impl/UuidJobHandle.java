/**
 * 
 */
package org.drools.grid.timer.impl;

import java.io.Serializable;
import java.util.UUID;

import org.drools.time.JobHandle;

public class UuidJobHandle
    implements
    JobHandle,
    Serializable {
    private UUID uuid;

    public UuidJobHandle() {
        this.uuid = UUID.randomUUID();
    }

    public UuidJobHandle(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        if ( result < 0 ) {
            result *= -1;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        UuidJobHandle other = (UuidJobHandle) obj;
        if ( uuid == null ) {
            if ( other.uuid != null ) return false;
        } else if ( !uuid.equals( other.uuid ) ) return false;
        return true;
    }

    public UUID getUuid() {
        return this.uuid;
    }
}