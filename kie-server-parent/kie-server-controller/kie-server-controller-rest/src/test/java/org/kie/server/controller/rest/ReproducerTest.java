package org.kie.server.controller.rest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;

import static org.kie.server.controller.rest.ControllerUtils.marshal;

public class ReproducerTest {

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();
    private static final String serverTemplateId = "id";

    private CountDownLatch serverUp = new CountDownLatch(1);

    @Before
    public void setUp() {
        ServerTemplate serverTemplate = new ServerTemplate(serverTemplateId, "name");
        IntStream.range(0, 1000)
                 .boxed()
                 .map(i -> Integer.toString(i))
                 .map(s -> new ServerInstanceKey(s, s, s, ""))
                 .forEach(instance -> serverTemplate.addServerInstance(instance));
        templateStorage.store(serverTemplate);
    }

    @Test
    public void testConcurrency() throws InterruptedException {
        final ServerTemplate serverTemplate = templateStorage.load(serverTemplateId);

        Thread disconnect = new Thread(() -> disconnect());
        disconnect.start();

        // Sync before marshalling
        serverUp.await(5, TimeUnit.SECONDS);
        String response = marshal("application/xml", serverTemplate);
        System.out.println(response);
    }

    public void disconnect() {
        ServerTemplate serverTemplate = templateStorage.load(serverTemplateId);

        try {
            serverUp.countDown();
            for (ServerInstanceKey instanceKey : serverTemplate.getServerInstanceKeys()) {
                serverTemplate.deleteServerInstance(instanceKey.getServerInstanceId());
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
