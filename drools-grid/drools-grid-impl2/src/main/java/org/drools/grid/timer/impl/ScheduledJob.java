/**
 * 
 */
package org.drools.grid.timer.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.drools.time.Job;
import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import org.drools.time.Trigger;

public class ScheduledJob implements Externalizable {
    private static final long serialVersionUID = 510l;

    private String            id;
    private UuidJobHandle     jobHandle;
    private Class             jobClass;
    private Serializable      trigger;
    private Date              nextFireTime;
    private ScheduledJobConfiguration configuration;
    private Serializable      ctx;
    
    public ScheduledJob() {
        
    }

    
    public ScheduledJob(UuidJobHandle jobHandle,
                        final Job job,
                        final JobContext context,
                        final Trigger trigger) {
        this.jobHandle = jobHandle;
        this.id = jobHandle.getUuid().toString();
        this.jobClass = job.getClass();
        this.ctx = (Serializable) context;
        this.trigger = (Serializable) trigger;
        this.nextFireTime = trigger.hasNextFireTime();
        
    }
    /**
     * @param jhandle 
     * @param timestamp
     * @param behavior
     * @param behaviorContext 
     */
    public ScheduledJob(UuidJobHandle jobHandle,
                        final Job job,
                        final JobContext context,
                        final Trigger trigger,
                        final ScheduledJobConfiguration conf) {
        this(jobHandle, job, context, trigger);
        this.configuration = conf;
    }

    public JobHandle getJobHandle() {
        if ( this.jobHandle == null ) {
            // it's transient on persistence, so restore on demand
            this.jobHandle = new UuidJobHandle( UUID.fromString( this.id ));
        }
        return this.jobHandle;
    }

    public Job getJob() {
        try {
            return (Job) jobClass.newInstance();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }
    
    public String getId() {
        return this.id;
    }
    
    public Date getNextFireTime() {
        return this.nextFireTime;
    }

    public Trigger getTrigger() {
        return (Trigger) trigger;
    }

    public JobContext getJobContext() {
        return (JobContext) ctx;
    }

    public ScheduledJobConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ScheduledJobConfiguration configuration) {
        this.configuration = configuration;
    }
    
    

    public String toString() {
        return "ScheduledJob( job=" + jobClass.getName() + " trigger=" + trigger + " context=" + ctx + " )";
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF( this.id );
        out.writeUTF( this.jobClass.getCanonicalName() );
        out.writeObject( this.trigger );
        out.writeLong( this.nextFireTime.getTime() );
        out.writeObject( this.ctx );
    }
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        this.id = in.readUTF();
        this.jobClass = ( Class ) Class.forName(in.readUTF());
        this.trigger = ( Serializable ) in.readObject();
        this.nextFireTime = new Date( in.readLong() );
        this.ctx = ( Serializable ) in.readObject();
    }


}