/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.client.serialization.jaxb.impl.type;

import java.lang.reflect.Array;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "array-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbArray implements JaxbType<Object[]> {

    @XmlElement
    private Object [] value;

    public JaxbArray() {

    }

    public JaxbArray(Object objValue) {
        Class objClass = objValue.getClass();
        if( ! objClass.isArray() ) {
           throw new IllegalArgumentException("This wrapper can only wrap arrays, not instances of "+ objClass.getName() );
        }
        int length = Array.getLength(objValue);
        this.value = new Object[length];
        for( int i = 0; i < length; ++i ) {
           Array.set(this.value, i, Array.get(objValue, i));
        }
    }

    @Override
    public Object [] getValue() {
        return  value;
    }

    @Override
    public void setValue( Object [] value ) {
        this.value = value;
    }
}
