package org.drools.osgi.tests.felix;

import javax.inject.Inject;

import org.drools.osgi.tests.common.TestFrameworkBundlesAssertions;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

/**
 * Abstract test with all initializations
 * 
 * @author Cristiano Gavião
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class AbstractTest {
	/**
	 * Injected BundleContext
	 */
	@Inject
	protected BundleContext bc;

	private TestFrameworkBundlesAssertions testAssertions;

	@Before
	public void setupTest() {
		testAssertions = new TestFrameworkBundlesAssertions();
	}

	public TestFrameworkBundlesAssertions getTestAssertions() {
		return testAssertions;
	}
}
