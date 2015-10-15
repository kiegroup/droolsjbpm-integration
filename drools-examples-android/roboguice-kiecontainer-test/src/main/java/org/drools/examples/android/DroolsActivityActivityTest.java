package org.drools.examples.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import android.widget.Button;
import android.widget.TextView;
import com.cnh.pf.product.test.AllTests;
import org.junit.Assert;

import org.drools.examples.android.roboguice.kiecontainer.R;

import static org.junit.Assert.assertNotNull;

/**
 * This demonstrates completely isolated "unit test" of an Activity class.
 * <p>
 * This model for testing creates the entire Activity (like
 * {@link DroolsActivityInstrumentationTest}) but does not attach it to the system (for
 * example, it cannot launch another Activity). It allows you to inject
 * additional behaviors via the
 * {@link android.test.ActivityUnitTestCase#setActivityContext(Context)} and
 * {@link android.test.ActivityUnitTestCase#setApplication(android.app.Application)}
 * methods. It also allows you to more carefully test your Activity's
 * performance Writing unit tests in this manner requires more care and
 * attention, but allows you to test very specific behaviors, and can also be an
 * easier way to test error conditions.
 * <p>
 * Because ActivityUnitTestCase creates the Activity under test completely
 * outside of the usual system, tests of layout and point-click UI interaction
 * are much less useful in this configuration. It's more useful here to
 * concentrate on tests that involve the underlying data model, internal
 * business logic, or exercising your Activity's life cycle.
 * <p>
 * See {@link AllTests} for
 * documentation on running all tests and individual tests in this application.
 */
public class DroolsActivityActivityTest extends ActivityUnitTestCase<DroolsActivity> {

    private Intent mStartIntent;

    public DroolsActivityActivityTest() {
        super(DroolsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }

    /**
     * This test demonstrates ways to exercise the Activity's life cycle.
     */
    @MediumTest
    public void testLifeCycleCreate() {
        Activity activity = startActivity(mStartIntent, null, null);

        // At this point, onCreate() has been called
        getInstrumentation().callActivityOnStart(activity);
        getInstrumentation().callActivityOnResume(activity);

        assertNotNull("kiecontainer", ((DroolsActivity)activity).mContainer);
        assertNotNull("kiebase", ((DroolsActivity)activity).mKieBase);
        assertNotNull("kiesession", ((DroolsActivity)activity).kSession);
        Button fireRulesButton = (Button) activity.findViewById(R.id.fireRules);
        assertNotNull("fireRulesButton", fireRulesButton);
        TextView log = (TextView) activity.findViewById(R.id.log);
        fireRulesButton.performClick();

        // At this point you could use a Mock Context to confirm that
        //your activity has made certain calls to the system & set itself up properly.
        getInstrumentation().callActivityOnPause(activity);

        // At this point you could confirm that the activity has paused
        getInstrumentation().callActivityOnStop(activity);

        // At this point, you could confirm that the activity has shut itself down
        // or you could use a Mock Context to confirm that your activity has
        // released any system resources it should no longer be holding.
    }
}
