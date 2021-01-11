/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.kafka;

import java.util.Date;

class CloudEvent<T> {
    private String specversion;
    private Date time;
    private String id;
    private String type;
    private String source;
    private T data;

    public String getSpecversion() {
        return specversion;
    }

    public Date getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public T getData() {
        return data;
    }

    public void setSpecversion(String specversion) {
        this.specversion = specversion;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CloudEvent [specversion=" + specversion + ", time=" + time + ", id=" + id + ", type=" + type +
               ", source=" + source + ", data=" + data + "]";
    }
}
