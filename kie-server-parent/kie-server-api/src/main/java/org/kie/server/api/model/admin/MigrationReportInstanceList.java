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

package org.kie.server.api.model.admin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "migration-report-instance-list")
public class MigrationReportInstanceList implements ItemList<MigrationReportInstance> {

    @XmlElement(name = "migration-report-instance")
    private MigrationReportInstance[] migrationReportInstances;

    public MigrationReportInstanceList() {
    }

    public MigrationReportInstanceList(MigrationReportInstance[] migrationReportInstances) {
        this.migrationReportInstances = migrationReportInstances;
    }

    public MigrationReportInstanceList(List<MigrationReportInstance> migrationReportInstances) {
        this.migrationReportInstances = migrationReportInstances.toArray(new MigrationReportInstance[migrationReportInstances.size()]);
    }

    public MigrationReportInstance[] getMigrationReportInstances() {
        return migrationReportInstances;
    }

    public void setMigrationReportInstances(MigrationReportInstance[] migrationReportInstances) {
        this.migrationReportInstances = migrationReportInstances;
    }

    @Override
    public List<MigrationReportInstance> getItems() {
        if (migrationReportInstances == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(migrationReportInstances);
    }
}
