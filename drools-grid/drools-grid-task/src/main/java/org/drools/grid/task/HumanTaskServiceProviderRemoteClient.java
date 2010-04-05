/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.task;

import org.drools.grid.generic.GenericNodeConnector;

/**
 *
 * @author salaboy
 */
public class HumanTaskServiceProviderRemoteClient implements HumanTaskFactoryService {

    private GenericNodeConnector connector;
    private int id;

    public HumanTaskServiceProviderRemoteClient() {
    }

    public HumanTaskServiceProviderRemoteClient(GenericNodeConnector connector, int id) {
        this.connector = connector;
        this.id = id;
    }

    public GenericNodeConnector getConnector() {
        return connector;
    }

    public void setConnector(GenericNodeConnector connector) {
        this.connector = connector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    //@Override
    public HumanTaskService newHumanTaskService() {

        
            return new HumanTaskServiceImpl(this.connector, this.id);
      
    }



}
