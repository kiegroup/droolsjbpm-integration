/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.rest.query.helpers;

import java.util.Date;

import org.kie.api.task.model.Status;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder;

/**
 * This class contains all of the methods that add a specific query criteria to the 
 * {@link RemoteServicesQueryCommandBuilder} instances. 
 * </p> 
 * Some of these methods must be overridden by the {@link InternalTaskQueryHelper} or
 * {@link InternalProcInstQueryHelper} implementation.
 */
abstract class InternalQueryBuilderMethods {

    private RemoteServicesQueryCommandBuilder [] queryBuilders;
    
    protected void setQueryBuilders(RemoteServicesQueryCommandBuilder... queryBuilders) { 
        this.queryBuilders = queryBuilders;
    }
    
    protected RemoteServicesQueryCommandBuilder [] getQueryBuilders() { 
        return queryBuilders;
    }
    
    // query builder methods ------------------------------------------------------------------------------------------------------
    
    public void processInstanceId(long[] longData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.processInstanceId(longData);
        } 
    }
    
    public void processInstanceIdMin(long[] longData) {
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.processInstanceIdMin(longData[0]);
        } 
    }
    
    public void processInstanceIdMax(long[] longData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.processInstanceIdMax(longData[0]);
        } 
    }
    
    public void processId(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.processId(data);
        } 
    }
    
    public void deploymentId(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.deploymentId(data);
        } 
    }
    
    public void taskId(long[] longData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.taskId(longData);
        } 
    }
    
    public void taskIdMin(long longData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.taskIdMin(longData);
        } 
    }
    
    public void taskIdMax(long longData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.taskIdMax(longData);
        } 
    }
    
    public void initiator(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.initiator(data);
        } 
    }
    
    public void stakeHolder(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.stakeHolder(data);
        } 
    }
    
    public void potentialOwner(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.potentialOwner(data);
        } 
    }
    
    public void taskOwner(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.taskOwner(data);
        } 
    }
    
    public void businessAdmin(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.businessAdmin(data);
        } 
    }
    
    public void taskStatus(Status[] statuses) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.taskStatus(statuses);
        } 
    }
    
    public void processInstanceStatus(int[] intData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.processInstanceStatus(intData);
        } 
    }
    
    public void processVersion(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.processVersion(data);
        } 
    }
    
    public void startDateMin(Date date) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.startDateMin(date);
        } 
    }
    
    public void startDateMax(Date date) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.startDateMax(date);
        } 
    }
    
    public void startDate(Date[] dateData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.startDate(dateData);
        } 
    }
    
    public void endDateMin(Date date) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.endDateMin(date);
        } 
    }
    
    public void endDateMax(Date date) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.endDateMax(date);
        } 
    }
    
    public void endDate(Date[] dateData) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.endDate(dateData);
        } 
    }
    
    public void variableId(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.variableId(data);
        } 
    }
    
    public void value(String[] data) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.value(data);
        } 
    }
    
    public void variableValue(String varId, String value) { 
        for( RemoteServicesQueryCommandBuilder queryBuilder : getQueryBuilders() ) { 
            queryBuilder.variableValue(varId, value);
        } 
    }

}
