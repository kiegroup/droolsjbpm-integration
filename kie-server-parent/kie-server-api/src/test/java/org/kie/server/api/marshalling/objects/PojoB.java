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

@XmlRootElement(name = "pojo-b")
public class PojoB implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private List<PojoC> pojoCList;

    public PojoB() {
    }

    public PojoB( String name ) {
        this.name = name;
    }

    public List<PojoC> getPojoCList() {
        return pojoCList;
    }

    public void setPojoCList( List<PojoC> pojoCList ) {
        this.pojoCList = pojoCList;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PojoB [name=" + name + ", pojoCList=" + pojoCList + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pojoCList == null) ? 0 : pojoCList.hashCode());
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
        PojoB other = (PojoB) obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        } else if ( !name.equals( other.name ) )
            return false;
        if ( pojoCList == null ) {
            if ( other.pojoCList != null )
                return false;
        } else if ( !pojoCList.equals( other.pojoCList ) )
            return false;
        return true;
    }

}
