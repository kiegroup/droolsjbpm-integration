/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.inject.AbstractModule;
import org.drools.android.DroolsAndroidContext;
import org.drools.android.robolectric.ShadowBaseDexClassLoader;
import org.drools.android.robolectric.ShadowDexClassLoader;
import org.drools.android.robolectric.ShadowMultiDexClassLoader;
import org.drools.examples.android.roboguice.kiecontainer.R;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", shadows = {ShadowMultiDexClassLoader.class, ShadowDexClassLoader.class, ShadowBaseDexClassLoader.class})
public class DroolsActivityTest {

    @Before
    public void setup() {

    }

    @After
    public void teardown() {
    }

    //   @Test
    public void testInjection() throws Exception {
        DroolsAndroidContext.setContext(Robolectric.application);
        DroolsActivity activity = Robolectric.setupActivity(DroolsActivity.class);

        //assert injection worked
        assertNotNull("kiecontainer", activity.mContainer);
        assertNotNull("kiebase", activity.mKieBase);
        assertNotNull("kiesession", activity.kSession);
        View fireRulesButton = activity.findViewById(R.id.fireRules);
        Robolectric.clickOn(fireRulesButton);
        Robolectric.runBackgroundTasks();
        TextView logView = (TextView) activity.findViewById(R.id.log);
        assertTrue("logView contains 'Hello World'", logView.getText().toString().contains("Hello World"));
    }

    public class MyTestModule extends AbstractModule {

        @Override
        protected void configure() {
        }
    }
}
