/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.api.marshalling.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemsType", propOrder = {
    "standardItemsAndFreeformItems"
})
public class ItemsType
    implements Serializable, Cloneable
{

    private final static long serialVersionUID = 1L;
    @XmlElements({
        @XmlElement(name = "standardItem", type = StandardItemType.class),
        @XmlElement(name = "freeformItem", type = FreeFormItemType.class)
    })
    protected List<Serializable> standardItemsAndFreeformItems;

    public List<Serializable> getStandardItemsAndFreeformItems() {
        if (standardItemsAndFreeformItems == null) {
            standardItemsAndFreeformItems = new ArrayList<Serializable>();
        }
        return this.standardItemsAndFreeformItems;
    }


}