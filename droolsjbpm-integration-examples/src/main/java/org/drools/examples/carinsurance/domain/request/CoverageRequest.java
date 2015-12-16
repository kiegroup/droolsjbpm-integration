/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.carinsurance.domain.request;

import java.util.ArrayList;
import java.util.List;

import org.drools.examples.carinsurance.domain.policy.CoverageType;

public class CoverageRequest {

    private PolicyRequest policyRequest;
    private CoverageType coverageType;

    private boolean automaticallyDisapproved = false;
    private List<String> disapprovalMessageList = new ArrayList<String>();

    public CoverageRequest() {
    }

    public CoverageRequest(CoverageType coverageType) {
        this.coverageType = coverageType;
    }

    public PolicyRequest getPolicyRequest() {
        return policyRequest;
    }

    public void setPolicyRequest(PolicyRequest policyRequest) {
        this.policyRequest = policyRequest;
    }

    public CoverageType getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(CoverageType coverageType) {
        this.coverageType = coverageType;
    }

    public boolean isAutomaticallyDisapproved() {
        return automaticallyDisapproved;
    }

    public void setAutomaticallyDisapproved(boolean automaticallyDisapproved) {
        this.automaticallyDisapproved = automaticallyDisapproved;
    }

    public List<String> getDisapprovalMessageList() {
        return disapprovalMessageList;
    }

    // ############################################################################
    // Non getters and setters
    // ############################################################################

    public void addDisapprovalMessage(String disapprovalMessage) {
        disapprovalMessageList.add(disapprovalMessage);
    }
    
}
