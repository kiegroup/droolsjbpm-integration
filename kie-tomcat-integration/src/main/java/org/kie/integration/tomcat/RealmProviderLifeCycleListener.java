/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.integration.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

public class RealmProviderLifeCycleListener implements LifecycleListener {

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        Lifecycle lifecycle = lifecycleEvent.getLifecycle();

        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            if (lifecycle instanceof Container) {
                TomcatRealmLoginModule.setRealm(((Container) lifecycle).getRealm());
            }
        }
    }
}
