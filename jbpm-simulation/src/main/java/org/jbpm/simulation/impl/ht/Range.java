package org.jbpm.simulation.impl.ht;

import java.util.Calendar;

import org.joda.time.Interval;

public class Range implements Comparable<Range> {

    private int start;
    private int end;
    
    private Calendar localCalendar;
    private Interval interval;
    private AllocatedResources resources;
    
    public Range(int start, int end, int resourcePoolSize) {
        this.start = start;
        this.end = end;
        this.resources = new AllocatedResources(resourcePoolSize);
        this.localCalendar = Calendar.getInstance();
    }
    
    public boolean contains(long time) {
        localCalendar.setTimeInMillis(time);
        localCalendar.set(Calendar.HOUR_OF_DAY, start);
        localCalendar.set(Calendar.MINUTE, 0);
        long startInstant = localCalendar.getTimeInMillis();
        
        localCalendar.set(Calendar.HOUR_OF_DAY, end);
        long endInstant = localCalendar.getTimeInMillis();
        interval = new Interval(startInstant, endInstant);
        
        return interval.contains(time);
        
    }
    
    public AllocatedWork allocate(long startTime, long duration) {
        AllocatedWork allocatedWork = this.resources.allocate(startTime, duration, interval.getEndMillis());
        
        return allocatedWork;
    }
    
    public Interval getInterval() {
        return this.interval;
    }
    
    public int compareTo(Range o) {
        
        return this.end - o.start;
    }

}
