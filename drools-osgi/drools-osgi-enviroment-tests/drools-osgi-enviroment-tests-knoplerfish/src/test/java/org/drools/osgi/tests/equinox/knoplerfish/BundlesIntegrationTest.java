package org.drools.osgi.tests.equinox.knoplerfish;

import static org.ops4j.pax.exam.CoreOptions.options;

import org.drools.osgi.tests.common.CommonPaxExamConfiguration;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

/**
 * BundleAssert test
 * 
 * @author Cristiano Gavião
 */
public class BundlesIntegrationTest extends AbstractTest {

	@Configuration
	public static Option[] customTestConfiguration() {
		return options(
				CommonPaxExamConfiguration.baseDroolsConfiguration());
	}

	@Test
	public void assertTestFrameworkBundles() {
		testAssertions.assertTestBundlesAreInstalled();
	}
}
