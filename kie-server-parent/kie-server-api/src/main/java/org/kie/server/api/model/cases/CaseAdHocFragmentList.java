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
@XmlRootElement(name = "case-adhoc-fragment-list")
public class CaseAdHocFragmentList implements ItemList<CaseAdHocFragment> {

    @XmlElement(name="fragments")
    private CaseAdHocFragment[] adHocFragments;

    public CaseAdHocFragmentList() {
    }

    public CaseAdHocFragmentList(CaseAdHocFragment[] adHocFragments) {
        this.adHocFragments = adHocFragments;
    }

    public CaseAdHocFragmentList(List<CaseAdHocFragment> adHocFragments) {
        this.adHocFragments = adHocFragments.toArray(new CaseAdHocFragment[adHocFragments.size()]);
    }

    public CaseAdHocFragment[] getStages() {
        return adHocFragments;
    }

    public void setStages(CaseAdHocFragment[] adHocFragments) {
        this.adHocFragments = adHocFragments;
    }

    @Override
    public List<CaseAdHocFragment> getItems() {
        if (adHocFragments == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(adHocFragments);
    }
}
