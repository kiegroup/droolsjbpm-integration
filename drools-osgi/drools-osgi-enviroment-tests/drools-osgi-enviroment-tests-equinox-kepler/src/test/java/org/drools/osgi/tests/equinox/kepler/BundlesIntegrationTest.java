package org.drools.osgi.tests.equinox.kepler;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
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
				CommonPaxExamConfiguration.baseDroolsConfiguration(),
				mavenBundle().groupId("org.eclipse.birt.runtime")
						.artifactId("org.eclipse.osgi.services")
						.version("3.3.100.v20120522-1822"));
	}


	@Test
	public void assertTestFrameworkBundles() {
		testAssertions.assertTestBundlesAreInstalled();
	}
}
