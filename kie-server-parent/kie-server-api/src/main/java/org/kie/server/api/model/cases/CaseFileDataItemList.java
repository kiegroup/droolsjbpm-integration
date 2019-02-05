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

package org.kie.server.api.model.cases;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-file-data-item-list")
public class CaseFileDataItemList implements ItemList<CaseFileDataItem> {

    @XmlElement(name="instances")
    private CaseFileDataItem[] caseFileDataItems;

    public CaseFileDataItemList() {
    }

    public CaseFileDataItemList(CaseFileDataItem[] caseFileDataItems) {
        this.caseFileDataItems = caseFileDataItems;
    }

    public CaseFileDataItemList(List<CaseFileDataItem> caseFileDataItems) {
        this.caseFileDataItems = caseFileDataItems.toArray(new CaseFileDataItem[caseFileDataItems.size()]);
    }

    public CaseFileDataItem[] getCaseFileDataItems() {
        return caseFileDataItems;
    }

    public void setCaseFileDataItems(CaseFileDataItem[] caseFileDataItems) {
        this.caseFileDataItems = caseFileDataItems;
    }

    @Override
    public List<CaseFileDataItem> getItems() {
        if (caseFileDataItems == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(caseFileDataItems);
    }
}
