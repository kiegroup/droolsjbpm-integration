/*
 * Copyright 2012 JBoss Inc
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

import org.drools.examples.carinsurance.domain.Car;
import org.drools.examples.carinsurance.domain.Driver;
import org.drools.examples.carinsurance.domain.policy.Policy;

import java.util.ArrayList;
import java.util.List;

public class PolicyRequest {

    private Driver owner;
    private Car car;
    private List<CoverageRequest> coverageRequestList = new ArrayList<CoverageRequest>();

    private boolean automaticallyDisapproved = false;
    private List<String> disapprovalMessageList = new ArrayList<String>();
    private boolean requiresManualApproval = false;
    private boolean manuallyApproved = false;

    public PolicyRequest() {
    }

    public PolicyRequest(Driver owner, Car car) {
        this.car = car;
        this.owner = owner;
    }

    public Driver getOwner() {
        return owner;
    }

    public void setOwner(Driver owner) {
        this.owner = owner;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public List<CoverageRequest> getCoverageRequestList() {
        return coverageRequestList;
    }

    public boolean isAutomaticallyDisapproved() {
        return automaticallyDisapproved;
    }

    public void setAutomaticallyDisapproved(boolean automaticallyDisapproved) {
        this.automaticallyDisapproved = automaticallyDisapproved;
    }

    public boolean isRequiresManualApproval() {
        return requiresManualApproval;
    }

    public void setRequiresManualApproval(boolean requiresManualApproval) {
        this.requiresManualApproval = requiresManualApproval;
    }

    public boolean isManuallyApproved() {
        return manuallyApproved;
    }

    public void setManuallyApproved(boolean manuallyApproved) {
        this.manuallyApproved = manuallyApproved;
    }

    public List<String> getDisapprovalMessageList() {
        return disapprovalMessageList;
    }

    // ############################################################################
    // Non getters and setters
    // ############################################################################
    
    public void addCoverageRequest(CoverageRequest coverageRequest) {
        coverageRequest.setPolicyRequest(this);
        coverageRequestList.add(coverageRequest);
    }

    public void addDisapprovalMessage(String disapprovalMessage) {
        disapprovalMessageList.add(disapprovalMessage);
    }
    
}
