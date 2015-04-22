package org.drools.examples.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import roboguice.activity.RoboSplashActivity;

/**
 * @author kedzie
 */
public class SplashActivity extends RoboSplashActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressBar p = new ProgressBar(this);
        p.setIndeterminate(true);
        setContentView(p);
    }

    @Override
    protected void startNextActivity() {
        startActivity(new Intent(this, DroolsActivity.class));
    }
}
