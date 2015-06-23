/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.api.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "list-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbList implements Wrapped<List<?>> {

    @XmlElementWrapper(name = "items")
    private Object[] items;

    public JaxbList() {
    }

    public JaxbList(List<Object> items) {
        this.items = items.toArray();
    }

    public List<Object> getItems() {
        return Arrays.asList(items);
    }

    public void setItems(List<Object> items) {
        this.items = items.toArray();
    }

    @Override
    public List<?> unwrap() {
        List<Object> unwrapped = new ArrayList<Object>();

        if (items != null) {
            for (Object o : items) {
                Object item = o;
                if (o instanceof Wrapped) {
                    item = ((Wrapped) o).unwrap();
                }
                unwrapped.add(item);
            }
        }
        return unwrapped;
    }
}
