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
package org.kie.server.api.marshalling.test.model.kit;

import java.io.Serializable;

public class ClassRecord implements Serializable {

    private static final long serialVersionUID = 5174127032991892033L;

    private ClassData wkLogFileData;
    private int actPerformedLength;
    private ClassLog wkFileNetLogFileData;

    public ClassRecord() {
    }

    public ClassRecord(int actPerformedLength, ClassData wkLogFileData) {
        this.wkLogFileData = wkLogFileData;
        this.actPerformedLength = actPerformedLength;
    }

    public ClassRecord(int actPerformedLength, ClassLog wkFileNetLogFileData) {
        this.wkFileNetLogFileData = wkFileNetLogFileData;
        this.actPerformedLength = actPerformedLength;
    }

    public ClassData getWkLogFileData() {
        return wkLogFileData;
    }

    public void setWkLogFileData(ClassData wkLogFileData) {
        this.wkLogFileData = wkLogFileData;
    }

    public int getActPerformedLength() {
        return actPerformedLength;
    }

    public void setActPerformedLength(int actPerformedLength) {
        this.actPerformedLength = actPerformedLength;
    }

    public ClassLog getWkFileNetLogFileData() {
        return wkFileNetLogFileData;
    }

    public void setWkFileNetLogFileData(ClassLog wkFileNetLogFileData) {
        this.wkFileNetLogFileData = wkFileNetLogFileData;
    }

}
