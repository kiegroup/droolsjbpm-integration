/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.samples.listeners;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.springframework.stereotype.Component;

@Component
public class SampleAgendaEventListener extends DefaultAgendaEventListener {

    private List<String> fired = new ArrayList<>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        fired.add(event.getMatch().getRule().getName());
    }

    public List<String> getFired() {
        return fired;
    }

    public void clear() {
        this.fired.clear();
    }
}
