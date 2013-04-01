package org.drools.osgi.tests.common;

import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleAvailable;
import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleState;
import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleUnavailable;

import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class TestFrameworkBundlesAssertions {

	public void assertTestBundlesAreInstalled() {

		// assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
		// is installed into OSGi framework
		assertBundleAvailable("org.knowhowlab.osgi.testing.utils");
		// assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
		// is installed into OSGi framework
		assertBundleState(Bundle.ACTIVE, "org.knowhowlab.osgi.testing.utils",
				5, TimeUnit.SECONDS);
		// assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
		// and version "1.2.0"
		// is installed into OSGi framework
		assertBundleAvailable("org.knowhowlab.osgi.testing.utils", new Version(
				"1.2.0"));
		assertBundleAvailable("org.drools.osgi.tests.common", new Version(
				"6.0.0"));
		// assert bundle with symbolic name "org.knowhowlab.osgi.testing.utils"
		// and version "2.0.0"
		// is not installed into OSGi framework
		assertBundleUnavailable("org.knowhowlab.osgi.testing.utils",
				new Version("2.0.0"));
//		assertBundleAvailable("This bundles was not installed.", "org.knowhowlab.osgi.testing.utils",
//				new Version("2.0.0"));

	}

}
