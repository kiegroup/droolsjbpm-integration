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

package org.drools.examples.carinsurance.domain;

public enum CarType {
    /**
     * Low risk for theft, high risk for injuries inside.
     */
    SMALL,
    /**
     * Normal risk
     */
    MEDIUM,
    /**
     * High risk for theft, low risk for injuries inside, high risk for injuries outside.
     */
    LARGE,
    /**
     * High risk for everything
     */
    SPORT,
    /**
     * High risk for everything
     */
    MUSCLE,
    /**
     * High risk for repair costs.
     */
    LUXURY
}
