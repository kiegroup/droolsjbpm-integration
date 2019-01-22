package org.kie.server.integrationtests.dmn;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMNMetricApplication {

    static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "function-definition",
            "1.0.0.Final");

    private static final String CONTAINER_1_ID = "function-definition";

    static Logger logger = LoggerFactory.getLogger(DMNMetricApplication.class);

    static KieServicesClient kieServicesClient;
    static DMNServicesClient dmnClient;

    public static class AppTestClass extends RestJmsSharedBaseIntegrationTest {
        static {
            kieServicesClient = createDefaultStaticClient();
            kieServicesClient.getServicesClient(DMNServicesClient.class);
        }
    }

    static {
        AppTestClass appTestClass = new AppTestClass();
    }

    public static void main(String[] args) {
        String uriString = TestConfig.getKieServerHttpUrl() + "/prometheus";
        logger.info("SERVER_URL: " + uriString);

        final int parallelism = 4;
        final ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        final CyclicBarrier started = new CyclicBarrier(parallelism);
        final Callable<Long> task = () -> {
            started.await();
            final Thread current = Thread.currentThread();
            long executions = 0;
            while (!current.isInterrupted()) {
                evaluateDMNWithPause();
                executions++;
            }
            return executions;
        };
        final ArrayList<Future<Long>> tasks = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            tasks.add(executor.submit(task));
        }
        executor.shutdown();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            tasks.forEach(future -> future.cancel(true));
        }));
    }

    private static void evaluateDMNWithPause() {
        DMNContext dmnContext = dmnClient.newContext();

        ThreadLocalRandom salaryRandom = ThreadLocalRandom.current();

        int a = salaryRandom.nextInt(1000, 100000 / 12);
        int b = salaryRandom.nextInt(1000, 100000 / 12);

        dmnContext.set("a", a);
        dmnContext.set("b", b);
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID, dmnContext);
    }
}