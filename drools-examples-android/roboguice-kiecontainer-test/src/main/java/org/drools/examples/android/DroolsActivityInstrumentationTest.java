package org.drools.examples.android;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

import android.widget.Button;
import com.cnh.pf.product.test.AllTests;

import org.drools.examples.android.roboguice.kiecontainer.R;

import static org.junit.Assert.assertNotNull;

/**
 * An example of an {@link ActivityInstrumentationTestCase2} of a specific activity {@link DroolsActivity}.
 * By virtue of extending {@link ActivityInstrumentationTestCase2}, the target activity is automatically
 * launched and finished before and after each test.  This also extends
 * {@link android.test.InstrumentationTestCase}, which provides
 * access to methods for sending events to the target activity, such as key and
 * touch events.  See {@link #sendKeys}.
 *
 * In general, {@link android.test.InstrumentationTestCase}s and {@link ActivityInstrumentationTestCase2}s
 * are heavier weight functional tests available for end to end testing of your
 * user interface.  When run via a {@link android.test.InstrumentationTestRunner},
 * the necessary {@link android.app.Instrumentation} will be injected for you to
 * user via {@link #getInstrumentation} in your tests.
 *
 * See {@link DroolsActivityActivityTest} for an example of an Activity unit test.
 *
 * See {@link AllTests} for documentation on running
 * all tests and individual tests in this application.
 */
public class DroolsActivityInstrumentationTest extends ActivityInstrumentationTestCase2<DroolsActivity>  {


    public DroolsActivityInstrumentationTest() {
        super(DroolsActivity.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityIntent(new Intent(getInstrumentation().getContext(), DroolsActivity.class));
    }
    
    @MediumTest
    public void testPreconditions() throws Throwable {
        final DroolsActivity a = getActivity();
        assertNotNull(a);
        assertNotNull("kiecontainer", a.mContainer);
        assertNotNull("kiebase", a.mKieBase);
        assertNotNull("kiesession", a.kSession);

        final Button fireRulesButton = (Button) a.findViewById(R.id.fireRules);
        assertNotNull("fireRulesButton", fireRulesButton);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                fireRulesButton.performClick();
            }
        });
    }
}
