package org.drools.examples.android;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

/**
 * @author kedzie
 */
public class App extends MultiDexApplication {

    @Override protected void attachBaseContext(Context base) {
        try {
            super.attachBaseContext(base);
        } catch (RuntimeException ignored) {
            // Multidex support doesn't play well with Robolectric yet
        }
    }
}
