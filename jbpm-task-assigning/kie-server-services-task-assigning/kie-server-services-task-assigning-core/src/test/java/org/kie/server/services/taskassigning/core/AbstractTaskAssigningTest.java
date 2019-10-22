package org.kie.server.services.taskassigning.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assume.assumeTrue;

public abstract class AbstractTaskAssigningTest {

    /**
     * System property for triggering the "turtle tests". This tests are part of the product but only executed when
     * this system property is set. It's up to the productization scripts to determine when this tests should be
     * executed or not. Developers can always trigger them locally if needed.
     */
    String RUN_TURTLE_TESTS = "runTurtleTests";

    /**
     * System property for triggering the "development only tests".
     * This tests are not intended be part of product the build tests and should not be part of the productization scripts,
     * since they are only useful for developers to test stuff locally during development (e.g. for executing random operations)
     * Don't abuse with the use this tests.
     */
    String RUN_DEVELOPMENT_ONLY_TESTS = "runDevelopmentOnlyTests";

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected void checkRunTurtleTests() {
        assumeTrue("true".equals(System.getProperty(RUN_TURTLE_TESTS)));
    }

    protected void checkRunDevelopmentOnlyTests() {
        assumeTrue("true".equals(System.getProperty(RUN_DEVELOPMENT_ONLY_TESTS)));
    }
}
