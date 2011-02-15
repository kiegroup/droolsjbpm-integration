/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */

package org.drools.grid.timer.impl;

import java.net.InetSocketAddress;
import org.drools.grid.Grid;

public class SchedulerServiceConfiguration
    implements
    ServiceConfiguration {
    private int                 redundancy = 1; //Default 1, 0 all
    private InetSocketAddress[] addresses;

    public SchedulerServiceConfiguration(InetSocketAddress[] addresses) {
        this.addresses = addresses;
    }

    public InetSocketAddress[] getServices(Grid grid) {
        //get addresses from the grid.. or whatever
        return addresses;
    }

    public int getRedundancy() {
        return this.redundancy;
    }

    public void setRedundancy(int redundancy) {
        this.redundancy = redundancy;
    }

}
