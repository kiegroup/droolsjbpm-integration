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

package org.kie.server.api.marshalling;

public enum MarshallingFormat {
    XSTREAM(0, "xstream"), JAXB(1, "xml"), JSON(2, "json");

    private final int id;
    private final String type;

    MarshallingFormat( int id, String type ) {
        this.id = id;
        this.type = type;
    }

    public int getId() { return id; }

    public String getType() {
        return type;
    }

    public static MarshallingFormat fromId( int id ) {
        switch ( id ) {
            case 0 : return XSTREAM;
            case 1 : return JAXB;
            case 2 : return JSON;
            default: return null;
        }
    }

    public static MarshallingFormat fromType( String type ) {
        if ("xstream".equalsIgnoreCase( type ) || "application/xstream".equalsIgnoreCase( type ) ) {
            return XSTREAM;
        } else if ("xml".equalsIgnoreCase( type ) || "application/xml".equalsIgnoreCase( type ) ) {
            return JAXB;
        } else if ("json".equalsIgnoreCase( type ) || "application/json".equalsIgnoreCase( type ) ) {
            return JSON;
        } else {
            return valueOf(type);
        }
    }
}
