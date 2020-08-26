package org.jbpm.springboot.samples.events.emitters;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.base.BaseEventCollection;
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
        latch.countDown();
    }

    @Override
    public void apply(Collection<InstanceView<?>> data) {
        log.info("apply {}", data);
        latch.countDown();
    }

    @Override
    public void drop(Collection<InstanceView<?>> data) {
        log.info("drop {}", data);
        latch.countDown();
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
}
