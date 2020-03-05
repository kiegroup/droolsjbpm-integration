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

package org.jbpm.springboot.samples.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.task.deadlines.NotificationListener;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.model.NotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CountDownLatchNotificationListener implements NotificationListener {

    private CountDownLatch latch;
    private List<NotificationEvent> eventsReceived;

    @Autowired
    private ProcessService processService;

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
