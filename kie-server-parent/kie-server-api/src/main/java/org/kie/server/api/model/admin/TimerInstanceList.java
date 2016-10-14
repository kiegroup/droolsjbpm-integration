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

package org.kie.server.api.model.admin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "timer-instance-list")
public class TimerInstanceList implements ItemList<TimerInstance> {

    @XmlElement(name="timer-instance")
    private TimerInstance[] timerInstances;

    public TimerInstanceList() {
    }

    public TimerInstanceList(TimerInstance[] timerInstances) {
        this.timerInstances = timerInstances;
    }

    public TimerInstanceList(List<TimerInstance> timerInstances) {
        this.timerInstances = timerInstances.toArray(new TimerInstance[timerInstances.size()]);
    }

    public TimerInstance[] getTimerInstances() {
        return timerInstances;
    }

    public void setTimerInstances(TimerInstance[] timerInstances) {
        this.timerInstances = timerInstances;
    }

    @Override
    public List<TimerInstance> getItems() {
        if (timerInstances == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(timerInstances);
    }
}
