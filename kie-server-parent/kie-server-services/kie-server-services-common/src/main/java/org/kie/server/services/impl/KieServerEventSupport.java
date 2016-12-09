/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerEventSupport {

    private static final Logger logger = LoggerFactory.getLogger(KieServerEventSupport.class);
    private static final ServiceLoader<KieServerEventListener> eventListenersLoader = ServiceLoader.load(KieServerEventListener.class);

    private List<KieServerEventListener> eventListeners = new ArrayList<>();

    public KieServerEventSupport() {
        eventListenersLoader.forEach(
                listener -> {
                    eventListeners.add(listener);
                    logger.debug("Found kie server event listener {}", listener);
                }
        );
    }

    public void fireBeforeServerStarted(KieServer kieServer) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().beforeServerStarted(kieServer);
            } while (iter.hasNext());
        }
    }

    public void fireAfterServerStarted(KieServer kieServer) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().afterServerStarted(kieServer);
            } while (iter.hasNext());
        }
    }

    public void fireBeforeServerStopped(KieServer kieServer) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().beforeServerStopped(kieServer);
            } while (iter.hasNext());
        }
    }

    public void fireAfterServerStopped(KieServer kieServer) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().afterServerStopped(kieServer);
            } while (iter.hasNext());
        }
    }

    public void fireBeforeContainerStarted(KieServer kieServer, KieContainerInstance containerInstance) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().beforeContainerStarted(kieServer, containerInstance);
            } while (iter.hasNext());
        }
    }

    public void fireAfterContainerStarted(KieServer kieServer, KieContainerInstance containerInstance) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().afterContainerStarted(kieServer, containerInstance);
            } while (iter.hasNext());
        }
    }

    public void fireBeforeContainerStopped(KieServer kieServer, KieContainerInstance containerInstance) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().beforeContainerStopped(kieServer, containerInstance);
            } while (iter.hasNext());
        }
    }

    public void fireAfterContainerStopped(KieServer kieServer, KieContainerInstance containerInstance) {
        final Iterator<KieServerEventListener> iter = eventListeners.iterator();
        if (iter.hasNext()) {
            do{
                iter.next().afterContainerStopped(kieServer, containerInstance);
            } while (iter.hasNext());
        }
    }
}
