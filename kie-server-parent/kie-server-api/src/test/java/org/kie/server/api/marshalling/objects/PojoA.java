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

package org.kie.server.api.marshalling.objects;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "pojo-a")
public class PojoA implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private List<PojoB> pojoBList;

    private List<String> stringList;

    public PojoA() {
    }

    public PojoA( String name ) {
        this.name = name;
    }

    public List<PojoB> getPojoBList() {
        return pojoBList;
    }

    public void setPojoBList( List<PojoB> pojoBList ) {
        this.pojoBList = pojoBList;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList( List<String> stringList ) {
        this.stringList = stringList;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PojoA [name=" + name + ", pojoBList=" + pojoBList + ", stringList=" + stringList + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pojoBList == null) ? 0 : pojoBList.hashCode());
        result = prime * result + ((stringList == null) ? 0 : stringList.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        PojoA other = (PojoA) obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        } else if ( !name.equals( other.name ) )
            return false;
        if ( pojoBList == null ) {
            if ( other.pojoBList != null )
                return false;
        } else if ( !pojoBList.equals( other.pojoBList ) )
            return false;
        if ( stringList == null ) {
            if ( other.stringList != null )
                return false;
        } else if ( !stringList.equals( other.stringList ) )
            return false;
        return true;
    }

}
