package org.jbpm.springboot.samples.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jbpm.services.task.deadlines.NotificationListener;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.model.NotificationEvent;
import org.springframework.stereotype.Component;

@Component
public class CountDownLatchNotificationListener implements NotificationListener {

    private CountDownLatch latch;
    private List<NotificationEvent> eventsReceived;

    public CountDownLatchNotificationListener() {
        this.eventsReceived = new ArrayList<NotificationEvent>();
    }

    public void configure(int threads) {
        this.latch = new CountDownLatch(threads);
    }

    @Override
    public void onNotification(NotificationEvent event, UserInfo userInfo) {
        this.eventsReceived.add(event);
        this.latch.countDown();
    }

    public CountDownLatch getCountDown() {
        return this.latch;
    }

    public  List<NotificationEvent> getEventsReceived() {
        return this.eventsReceived;
    }

    public void reset(){
        this.eventsReceived = new ArrayList<NotificationEvent>();
    }

}
