/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model.taskassigning;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "task-assigning-planning-item-list")
public class PlanningItemList implements ItemList<PlanningItem> {

    @XmlElement(name = "planningItems")
    private PlanningItem[] planningItems;

    public PlanningItemList() {
        //marshalling constructor
    }

    public PlanningItemList(PlanningItem[] planningItems) {
        this.planningItems = planningItems;
    }

    public PlanningItemList(List<PlanningItem> planningItems) {
        this.planningItems = planningItems != null ? planningItems.toArray(new PlanningItem[0]) : null;
    }

    public PlanningItem[] getPlanningItems() {
        return planningItems;
    }

    public void setPlanningItems(PlanningItem[] planningItems) {
        this.planningItems = planningItems;
    }

    @Override
    public List<PlanningItem> getItems() {
        if (planningItems == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(planningItems);
    }

    @Override
    public String toString() {
        return "PlanningItemList{" +
                "planningItems=" + Arrays.toString(planningItems) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanningItemList)) {
            return false;
        }
        PlanningItemList that = (PlanningItemList) o;
        return Arrays.equals(planningItems, that.planningItems);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(planningItems);
    }
}
