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

package org.kie.server.services.api;

/**
 * Policy that can be applied on KIE Server components on regular basis.
 * It can be to perform cleanup operation (like disposing old containers)
 * it can be time based restriction in using containers and more.
 */
public interface Policy {

    /**
     * Returns unique name of the policy so it can be referenced by name
     * @return name of the policy
     */
    String getName();

    /**
     * Returns interval (in milliseconds) how often the policy should be applied.
     * @return interval in milliseconds
     */
    long getInterval();

    /**
     * Performs operation to start the policy - is executed only once when the policy is created
     */
    void start();

    /**
     * Performs operation to stop the policy - is executed only once when the policy is destroyed
     */
    void stop();

    /**
     * Applies given policy on kie server. Actual operations depends on implementation
     * though they can do any operation based on given parameters.
     *
     * <code>kieServer</code> should be used to alter state of the kie server while <code>kieServerRegistry</code>
     * should be used to locate information to evaluate if policy can be applied
     *
     * @param kieServerRegistry registry of the kie server
     * @param kieServer actual instance representing kie server to perform operations on containers
     */
    void apply(KieServerRegistry kieServerRegistry, KieServer kieServer);
}
