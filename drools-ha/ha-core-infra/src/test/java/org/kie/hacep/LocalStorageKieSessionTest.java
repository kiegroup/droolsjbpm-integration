/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.hacep;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.kie.hacep.core.Bootstrap;
import org.kie.hacep.core.InfraFactory;
import org.kie.hacep.core.infra.election.State;
import org.kie.hacep.sample.kjar.Result;
import org.kie.hacep.sample.kjar.StockTickEvent;
import org.kie.remote.CommonConfig;
import org.kie.remote.RemoteEntryPoint;
import org.kie.remote.RemoteFactHandle;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.TopicsConfig;
import org.kie.remote.impl.consumer.Listener;
import org.kie.remote.impl.consumer.ListenerThread;
import org.kie.remote.util.KafkaRemoteUtil;

import static org.junit.Assert.*;
import static org.kie.remote.CommonConfig.getTestProperties;
import static org.kie.remote.impl.EntryPointUtil.DEFAULT_ENTRY_POINT;

@Ignore("https://issues.redhat.com/browse/DROOLS-5321")
public class LocalStorageKieSessionTest {

    RemoteKieSession session;

    @Before
    public void initTest() {
        EnvConfig config = EnvConfig.getDefaultEnvConfig().underTest(true).local(true);
        Bootstrap.startEngine(config);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        ListenerThread listenerThread = KafkaRemoteUtil.getListenerThread(TopicsConfig.getDefaultTopicsConfig(), config.isLocal(), getTestProperties());
        Listener listener = new Listener(getTestProperties(), listenerThread);
        session = InfraFactory.createRemoteKieSession(CommonConfig.getTestProperties(), listener, InfraFactory.getProducer(config.isLocal())); //RemoteKieSession.create(getTestProperties());
    }


    @After
    public void endTest() throws IOException {
        session.close();
        Bootstrap.stopEngine();
    }

    @Test(timeout = 10000)
    public void insertTest() throws ExecutionException, InterruptedException {

        assertEquals((Long) 0L, session.getFactCount().get());
        assertEquals((Long) 0L, session.fireAllRules().get());

        RemoteFactHandle<Result> resultRFH = session.insert(new Result("RHT"));
        assertEquals((Long) 0L, session.fireAllRules().get());

        RemoteFactHandle<StockTickEvent> stockRFH = session.insert(new StockTickEvent("RHT", 9.0));
        session.insert(new StockTickEvent("RHT", 14.0));

        assertFalse(session.getObject(stockRFH).get().isProcessed());

        assertEquals((Long) 3L, session.getFactCount().get());
        assertEquals((Long) 3L, session.fireAllRules().get());

        assertTrue(session.getObject(stockRFH).get().isProcessed());

        Assert.assertEquals(11.5, session.getObject(resultRFH).get().getValue());
    }

    @Test(timeout = 10000)
    public void updateTest() throws ExecutionException, InterruptedException {

        assertEquals((Long) 0L, session.getFactCount().get());
        assertEquals((Long) 0L, session.fireAllRules().get());

        StockTickEvent stock = new StockTickEvent("RHT", 9.0);
        RemoteFactHandle<StockTickEvent> stockRFH = session.insert(stock);

        assertFalse(session.getObject(stockRFH).get().isProcessed());

        assertEquals((Long) 1L, session.fireAllRules().get());

        assertTrue(session.getObject(stockRFH).get().isProcessed());

        stock.setPrice(10.0);
        session.update(stockRFH, stock);

        Assert.assertEquals(stock.getPrice(), session.getObject(stockRFH).get().getPrice(), 0.1);
    }

    @Test(timeout = 10000)
    public void deleteTest() throws ExecutionException, InterruptedException {

        assertEquals((Long) 0L, session.getFactCount().get());
        assertEquals((Long) 0L, session.fireAllRules().get());

        StockTickEvent stock = new StockTickEvent("RHT", 9.0);
        RemoteFactHandle<StockTickEvent> stockRFH = session.insert(stock);

        assertFalse(session.getObject(stockRFH).get().isProcessed());

        assertEquals((Long) 1L, session.fireAllRules().get());

        assertTrue(session.getObject(stockRFH).get().isProcessed());

        assertEquals((Long) 1L, session.getFactCount().get());
        session.delete(stockRFH);
        assertEquals((Long) 0L, session.getFactCount().get());
    }

    @Test(timeout = 20000)
    public void fireUntilHaltTest() throws ExecutionException, InterruptedException {

        assertEquals((Long) 0L, session.getFactCount().get());
        session.fireUntilHalt();

        StockTickEvent stock1 = new StockTickEvent("RHT", 9.0);
        assertFalse(stock1.isProcessed());

        RemoteFactHandle<StockTickEvent> stock1RFH = session.insert(stock1);

        assertTrue(session.getObject(stock1RFH).get().isProcessed());

        session.halt();

        StockTickEvent stock2 = new StockTickEvent("RHT", 11.0);
        assertFalse(stock2.isProcessed());

        RemoteFactHandle<StockTickEvent> stock2RFH = session.insert(stock2);

        assertFalse(session.getObject(stock2RFH).get().isProcessed());

        assertEquals((Long) 1L, session.fireAllRules().get());

        assertTrue(session.getObject(stock2RFH).get().isProcessed());
    }

    @Test(timeout = 10000)
    public void getCommandsTest() throws ExecutionException, InterruptedException {

        RemoteFactHandle<StockTickEvent> stock1FH = session.insert(new StockTickEvent("RHT", 9.0));
        session.insert(new StockTickEvent("RHT", 19.0));

        Collection<StockTickEvent> getObjectsByClass = session.getObjects(StockTickEvent.class).get();
        assertEquals(2, getObjectsByClass.size());

        Collection<?> getObjects = session.getObjects().get();
        assertEquals(2, getObjects.size());

        CompletableFuture<Collection> getObjectByQueryIBM = session.getObjects("stockTickEventQuery", "stock", "IBM");
        assertEquals(0, getObjectByQueryIBM.get().size());
        CompletableFuture<Collection> getObjectsByQueryRHT = session.getObjects("stockTickEventQuery", "stock", "RHT");
        assertEquals(2, getObjectsByQueryRHT.get().size());

        Assert.assertEquals("RHT", session.getObject(stock1FH).get().getCompany());

        RemoteEntryPoint defaultEntryPoint = session.getEntryPoint(DEFAULT_ENTRY_POINT);
        assertEquals((Long) 2L, defaultEntryPoint.getFactCount().get());

        assertEquals(DEFAULT_ENTRY_POINT, defaultEntryPoint.getEntryPointId());
    }

    @Test(timeout = 10000)
    public void getKJarNotDefinedTest() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cfGav = session.getKJarGAV();
        String gav = cfGav.get();
        assertNotNull(gav);
        assertEquals("KJar GAV NotDefined", gav);
    }

}