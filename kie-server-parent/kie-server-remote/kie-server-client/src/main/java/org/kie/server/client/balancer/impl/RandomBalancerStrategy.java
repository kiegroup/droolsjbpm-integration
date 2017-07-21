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

package org.kie.server.client.balancer.impl;

import java.util.ArrayList;
import java.util.List;

public class RandomBalancerStrategy extends AbstractBalancerStrategy {

    private List<String> availableEndpoints = new ArrayList<String>();

    public RandomBalancerStrategy(List<String> availableEndpoints) {
        availableEndpoints.forEach(endpoint -> markAsOnline(endpoint));
    }

    @Override
    public String next() {
        checkEmpty(availableEndpoints);

        if (availableEndpoints.size() == 1) {
            return availableEndpoints.get(0);
        }

        int index = getRandomInt(0, availableEndpoints.size()-1);

        return availableEndpoints.get(index);
    }

    @Override
    public String markAsOffline(String url) {
        synchronized (availableEndpoints) {
            String baseUrl = locateUrl(availableEndpoints, url);
        	
            availableEndpoints.remove(baseUrl);
            
            return baseUrl;
        }
    }

    @Override
    public String markAsOnline(String url) {
        synchronized (availableEndpoints) {
            String baseUrl = locateUrl(availableEndpoints, url);
        	
            if (!availableEndpoints.contains(baseUrl)) {
                availableEndpoints.add(baseUrl);
            }
            
            return baseUrl;
        }
    }

    @Override
    public List<String> getAvailableEndpoints() {
        return new ArrayList<String>(availableEndpoints);
    }

    protected int getRandomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1)) + min;
    }

    @Override
    public String toString() {
        return "RandomBalancerStrategy{" +
                "availableEndpoints=" + availableEndpoints +
                '}';
    }
}
