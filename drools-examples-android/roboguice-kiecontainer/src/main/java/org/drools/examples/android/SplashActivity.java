/*
 * Copyright 2015 JBoss by Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import roboguice.activity.RoboSplashActivity;

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
