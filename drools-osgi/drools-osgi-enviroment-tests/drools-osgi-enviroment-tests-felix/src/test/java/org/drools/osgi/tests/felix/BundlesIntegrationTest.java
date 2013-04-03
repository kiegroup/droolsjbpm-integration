package org.drools.osgi.tests.felix;

import static org.ops4j.pax.exam.CoreOptions.options;

import org.drools.osgi.tests.common.CommonPaxExamConfiguration;
import org.drools.osgi.tests.common.TestFrameworkBundlesAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * BundleAssert test
 * 
 * @author Cristiano Gavião
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BundlesIntegrationTest extends AbstractTest {

	@Configuration
	public static Option[] customTestConfiguration() {
		return options(CommonPaxExamConfiguration.baseDroolsConfiguration());
	}

	@Test
	public void assertTestFrameworkBundles() {
		TestFrameworkBundlesAssertions testAssertions = getTestAssertions();
		testAssertions.assertTestBundlesAreInstalled();
	}
}
