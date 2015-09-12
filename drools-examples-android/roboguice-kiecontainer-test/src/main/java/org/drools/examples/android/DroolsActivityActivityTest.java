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
    public void testLifeCycleCreate() throws InterruptedException {
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
        Thread.sleep(2000);
        assertFalse(log.getText().toString().isEmpty());

        getInstrumentation().callActivityOnPause(activity);
        getInstrumentation().callActivityOnStop(activity);
    }
}
