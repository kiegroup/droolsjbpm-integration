package org.drools.osgi.tests.core.test.model;

import org.drools.core.test.model.RuleBaseConfigurationTest;
import org.drools.osgi.tests.env.DroolsBundlesAssertions;
import org.drools.osgi.tests.env.FrameworkBundlesAssertions;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class RuleBaseConfigurationOsgiTest extends RuleBaseConfigurationTest {

    @Before
    public void checkRequisites() {
        FrameworkBundlesAssertions.assertTestBundlesAreInstalled();
        DroolsBundlesAssertions.assertDroolsBundlesAvailable();
        DroolsBundlesAssertions.assertDroolsBundlesActivated();
    }
}
