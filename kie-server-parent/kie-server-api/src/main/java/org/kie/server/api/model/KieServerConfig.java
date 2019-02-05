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

package org.kie.server.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XmlRootElement(name="kie-server-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class KieServerConfig {

    @XmlElement(name="config-items")
    @XStreamImplicit
    private List<KieServerConfigItem> configItems = new ArrayList<KieServerConfigItem>();

    public KieServerConfig() {
    }

    public KieServerConfig(List<KieServerConfigItem> configItems) {
        if (configItems != null) {
            this.configItems = configItems;
        }
    }

    public List<KieServerConfigItem> getConfigItems() {
        return configItems;
    }

    public void setConfigItems(List<KieServerConfigItem> configItems) {
        this.configItems = configItems;
    }

    public void addConfigItem(KieServerConfigItem configItem) {
        KieServerConfigItem existing = getConfigItem(configItem.getName());
        if (existing != null) {
            removeConfigItem(existing);
        }
        this.configItems.add(configItem);
    }

    public void removeConfigItem(KieServerConfigItem configItem) {
        this.configItems.remove(configItem);
    }

    public KieServerConfigItem getConfigItem(String name) {
        KieServerConfigItem configItem = null;

        for (KieServerConfigItem item : configItems) {
            if (name.equals(item.getName())) {
                configItem = item;
                break;
            }
        }

        return configItem;
    }

    public String getConfigItemValue(String name) {
        KieServerConfigItem item = getConfigItem(name);

        if (item != null) {
            return item.getValue();
        } else {
            return System.getProperty(name);
        }
    }

    public String getConfigItemValue(String name, String defaultValue) {
        String value = getConfigItemValue(name);
        return value == null ? defaultValue : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KieServerConfig that = (KieServerConfig) o;

        if (configItems != null ? !configItems.equals(that.configItems) : that.configItems != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return configItems != null ? configItems.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "KieServerConfig{" +
                "configItems=" + configItems +
                '}';
    }
}
