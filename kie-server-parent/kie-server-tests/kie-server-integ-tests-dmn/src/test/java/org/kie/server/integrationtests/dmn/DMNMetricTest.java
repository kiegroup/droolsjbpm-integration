/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.integrationtests.dmn;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class DMNMetricTest
        extends DMNKieServerBaseIntegrationTest {

    private static final ReleaseId kjar1 = new ReleaseId(
            "org.kie.server.testing", "function-definition",
            "1.0.0.Final");

    private static final String CONTAINER_1_ID = "function-definition";

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/function-definition");

        kieContainer = KieServices.Factory.get().newKieContainer(kjar1);
        createContainer(CONTAINER_1_ID, kjar1);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses)
            throws Exception {
    }

    private static Client httpClient;

    protected WebTarget newRequest(String uriString) {
        if (httpClient == null) {
            httpClient = new ResteasyClientBuilder()
                    .establishConnectionTimeout(10, TimeUnit.SECONDS)
                    .socketTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        WebTarget webTarget = httpClient.target(uriString);
        webTarget.register(new BasicAuthentication(TestConfig.getUsername(), TestConfig.getPassword()));
        return webTarget;
    }

    @Test
    public void test_evaluateAllWithMetrics() {
        DMNContext dmnContext = dmnClient.newContext();
        dmnContext.set("a", 10);
        dmnContext.set("b", 5);
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID, dmnContext);

        assertEquals(ResponseType.SUCCESS, evaluateAll.getType());

        Response response = null;
        try {
            String uriString = TestConfig.getKieServerHttpUrl() + "/prometheus";

            WebTarget clientRequest = newRequest(uriString);
            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();

            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String res = response.readEntity(String.class);
            logger.info("Response: " + res);
            Assert.assertThat(res, not(isEmptyOrNullString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Test
    public void test_evaluateAllWithMetricsLoop() throws InterruptedException {
        String uriString = TestConfig.getKieServerHttpUrl() + "/prometheus";
        logger.info("SERVER_URL: " + uriString);

        logger.info("Starting in four seconds");
        Thread.sleep(4000);


        while(true) {
            logger.info("1 thread: " + uriString);
            runParallel(1);
            Thread.sleep(10000);

            logger.info("2 thread: " + uriString);
            runParallel(2);
            Thread.sleep(10000);

            logger.info("4 thread: " + uriString);
            runParallel(4);
            Thread.sleep(10000);
        }

    }

    public void runParallel(int parallelism) {
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

    private void evaluateDMNWithPause() {
        DMNContext dmnContext = dmnClient.newContext();

        ThreadLocalRandom salaryRandom = ThreadLocalRandom.current();

        int a = salaryRandom.nextInt(1000, 100000 / 12);
        int b = salaryRandom.nextInt(1000, 100000 / 12);

        dmnContext.set("a", a);
        dmnContext.set("b", b);
        ServiceResponse<DMNResult> evaluateAll = dmnClient.evaluateAll(CONTAINER_1_ID, dmnContext);
//        logger.info("result" + evaluateAll);
    }



}
