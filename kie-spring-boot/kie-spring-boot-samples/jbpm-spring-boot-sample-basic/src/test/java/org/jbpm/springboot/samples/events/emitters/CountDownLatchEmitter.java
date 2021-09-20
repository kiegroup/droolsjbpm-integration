/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples.events.emitters;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.base.BaseEventCollection;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.jbpm.services.api.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CountDownLatchEmitter implements EventEmitter {

    private static final Logger log = LoggerFactory.getLogger(CountDownLatchEmitter.class);
    private CountDownLatch latch;

    public CountDownLatchEmitter() {
        this.latch = new CountDownLatch(0);
    }

    @Autowired
    private ProcessService processService;

    public void configure(int threads) {
        this.latch = new CountDownLatch(threads);
    }

    public ProcessService getProcessService() {
        return processService;
    }

    public CountDownLatch getCountDownLatch() {
        return latch;
    }

    @Override
    public void deliver(Collection<InstanceView<?>> data) {
        log.info("deliver {}", data);
        countDownIfTaskInstanceView(data);
    }

    @Override
    public void apply(Collection<InstanceView<?>> data) {
        log.info("apply {}", data);
        countDownIfTaskInstanceView(data);
    }

    @Override
    public void drop(Collection<InstanceView<?>> data) {
        log.info("drop {}", data);
        countDownIfTaskInstanceView(data);
    }

    @Override
    public EventCollection newCollection() {
        log.info("new Collection for Event Emitter");
        latch.countDown();
        return new BaseEventCollection();
    }

    @Override
    public void close() {
        log.info("closing Event Emitter");
        latch.countDown();
    }
    
    protected void countDownIfTaskInstanceView(Collection<InstanceView<?>> data) {
        if (data.stream().anyMatch(instanceView -> instanceView instanceof TaskInstanceView)) {
            //countDown just whether TaskInstanceView is present
            latch.countDown();
        } else {
            log.info("no TaskInstanceView, no countDown");
        }
    }
}
