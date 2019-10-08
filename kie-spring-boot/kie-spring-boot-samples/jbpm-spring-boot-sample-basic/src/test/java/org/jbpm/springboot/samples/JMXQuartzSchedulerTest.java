/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.springboot.samples.listeners.CountDownLatchEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { JBPMApplication.class,
		TestAutoConfiguration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-quartz.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class JMXQuartzSchedulerTest {

	private static final String GROUP_ID = "org.jbpm";
	private static final String ARTIFACT_ID = "intermediate-timer-sample";
	private static final String VERSION = "1.0";

	private static final String PROCESS_ID = "org.jbpm.sample.intermediate-timer";

	private KModuleDeploymentUnit unit = null;
	private MBeanServer mBeanServer = null;
	private ObjectName oName = null;

	@Autowired
	private ProcessService processService;

	@Autowired
	private DeploymentService deploymentService;

	@Autowired
	private CountDownLatchEventListener countDownListener;

	@BeforeClass
	public static void generalSetup() {
		KieServices ks = KieServices.Factory.get();
		org.kie.api.builder.ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
		File kjar = new File("../kjars/intermediate-timer-sample/intermediate-timer-sample-1.0.jar");
		File pom = new File("../kjars/intermediate-timer-sample/pom.xml");
		MavenRepository repository = getMavenRepository();
		repository.installArtifact(releaseId, kjar, pom);
	}

	@Before
	public void setup() {
		unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
		deploymentService.deploy(unit);		
		countDownListener.configure(PROCESS_ID, 1);
	}

	@After
	public void cleanup() {
		deploymentService.undeploy(unit);
	}

	@Test(timeout = 30000)
	public void whenSchedulerStartedThenTimerIsFired() throws Exception {
		long processInstanceId = processService.startProcess(unit.getIdentifier(), PROCESS_ID);
		assertNotNull(processInstanceId);

		countDownListener.getCountDown().await();
		assertTrue(countDownListener.getExecutingThread().startsWith("SpringBootScheduler"));

		ProcessInstance pi = processService.getProcessInstance(processInstanceId);
		assertNull(pi);
	}

	@Test(timeout = 30000)
	public void whenSchedulerPausedThenTimerNotFired() throws Exception {
		long processInstanceId = processService.startProcess(unit.getIdentifier(), PROCESS_ID);
		assertNotNull(processInstanceId);

		pauseScheduler();

		awaitWithoutInterruption();

		ProcessInstance pi = processService.getProcessInstance(processInstanceId);
		assertNotNull(processInstanceId);

		assertEquals(STATE_ACTIVE, pi.getState());

		processService.abortProcessInstance(processInstanceId);
	}

	@Test(timeout = 40000)
	public void whenSchedulerRestartedThenTimerIsFired() throws Exception {		
		long processInstanceId = processService.startProcess(unit.getIdentifier(), PROCESS_ID);
		assertNotNull(processInstanceId);

		pauseScheduler();
		
		awaitWithoutInterruption();
		
		restartScheduler();

		countDownListener.getCountDown().await();
		assertTrue(countDownListener.getExecutingThread().startsWith("SpringBootScheduler"));

		ProcessInstance pi = processService.getProcessInstance(processInstanceId);
		assertNull(pi);
	}
	
	private void awaitWithoutInterruption() throws InterruptedException {
		// Wait 20 seconds, as the timer was scheduled for 15 seconds
		boolean await = countDownListener.getCountDown().await(20, TimeUnit.SECONDS);
		assertFalse(await);
	}

	private void pauseScheduler() throws Exception {
		findTargetObjectName();

		waitUntilSchedulerStarted();

		mBeanServer.invoke(oName, "standby", null, null);

		assertFalse("There should be Started attribute set to false",
				(boolean) mBeanServer.getAttribute(oName, "Started"));
		assertTrue("There should be StandbyMode attribute set to true",
				(boolean) mBeanServer.getAttribute(oName, "StandbyMode"));
	}

	private void restartScheduler() throws Exception {
		mBeanServer.invoke(oName, "start", null, null);

		assertTrue("There should be Started attribute set to true",
				(boolean) mBeanServer.getAttribute(oName, "Started"));
		assertFalse("There should be StandbyMode attribute set to false",
				(boolean) mBeanServer.getAttribute(oName, "StandbyMode"));
	}

	private void findTargetObjectName() throws MalformedObjectNameException {
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objectNames = mBeanServer
				.queryNames(new ObjectName("quartz:type=QuartzScheduler,name=*,instance=*"), null);

		assertFalse("There should be a quartz scheduler MBean", objectNames.isEmpty());
		oName = objectNames.stream().findFirst().get();
	}

	private void waitUntilSchedulerStarted() {
		BooleanSupplier schedulerStarted = () -> {
			try {
				return (boolean) mBeanServer.getAttribute(oName, "Started");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		wait(Duration.of(30, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS), schedulerStarted);
	}

	private static void wait(Duration maxDuration, Duration waitStep, BooleanSupplier booleanSupplier) {
		Instant startTime = Instant.now();

		while (startTime.plus(maxDuration).isAfter(Instant.now()) && !booleanSupplier.getAsBoolean()) {
			wait(waitStep);
		}
	}

	private static void wait(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException("Waiting was interrupted", e);
		}
	}

}
