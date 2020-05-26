/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.upperCase;

public enum MarshallingFormat {
    XSTREAM(0, "xstream"),
    JAXB(1, "xml"),
    JSON(2, "json");

    private final int id;
    private final String type;

    MarshallingFormat(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public static MarshallingFormat fromId(int id) {
        switch (id) {
            case 0:
                return XSTREAM;
            case 1:
                return JAXB;
            case 2:
                return JSON;
            default:
                return null;
        }
    }

    
    public static boolean isStrictType(String type) {
        String strictParam  = (String)buildParameters(type).get(Marshaller.MARSHALLER_PARAMETER_STRICT);
        return strictParam != null && Boolean.parseBoolean(strictParam);
    }
    
    public static Map<String,Object> buildParameters(String contentType)
    {
        int idx;
        return ((idx = contentType.indexOf(';')) < 0 || (idx + 1) == contentType.length()) 
                ? Collections.emptyMap() 
                : Arrays.stream(contentType.substring(idx + 1).split(",")).
                    filter(e -> e.split("=").length > 1) // remove bad parameters
                    .map(e -> e.split("="))
                    .collect(Collectors.toMap(e -> e[0].trim(), e ->  e[1].trim()));
    }
    


    public static MarshallingFormat fromType(String type) {
        if (startsWithIgnoreCase(type, "xstream") || startsWithIgnoreCase(type, "application/xstream")) {
            return XSTREAM;
        } else if (startsWithIgnoreCase(type, "xml") || startsWithIgnoreCase(type, "application/xml")) {
            return JAXB;
        } else if (startsWithIgnoreCase(type, "json") || startsWithIgnoreCase(type, "application/json")) {
            return JSON;
        } else {
            try {
                return MarshallingFormat.valueOf(upperCase(type));
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Invalid marshalling format [" + type + "]");
        }
    }
}
