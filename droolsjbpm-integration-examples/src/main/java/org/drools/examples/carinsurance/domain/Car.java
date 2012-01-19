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

package org.drools.examples.carinsurance.domain;

import java.math.BigDecimal;

public class Car {
    
    private String vehicleIdentificationNumber;

    private CarType carType;
    private boolean antiTheftDevice;
    private BigDecimal value;

    public Car() {
    }

    public Car(String vehicleIdentificationNumber, CarType carType, boolean antiTheftDevice, BigDecimal value) {
        this.vehicleIdentificationNumber = vehicleIdentificationNumber;
        this.carType = carType;
        this.antiTheftDevice = antiTheftDevice;
        this.value = value;
    }

    public String getVehicleIdentificationNumber() {
        return vehicleIdentificationNumber;
    }

    public void setVehicleIdentificationNumber(String vehicleIdentificationNumber) {
        this.vehicleIdentificationNumber = vehicleIdentificationNumber;
    }

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }

    public boolean isAntiTheftDevice() {
        return antiTheftDevice;
    }

    public void setAntiTheftDevice(boolean antiTheftDevice) {
        this.antiTheftDevice = antiTheftDevice;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    // ############################################################################
    // Non getters and setters
    // ############################################################################

}
