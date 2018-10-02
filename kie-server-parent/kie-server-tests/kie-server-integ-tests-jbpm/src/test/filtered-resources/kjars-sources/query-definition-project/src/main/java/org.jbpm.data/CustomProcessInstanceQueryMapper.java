/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryResultMapper;


public class CustomProcessInstanceQueryMapper implements QueryResultMapper<List<ProcessInstanceDesc>> {
    
    public CustomProcessInstanceQueryMapper() {
    }
    
    public static CustomProcessInstanceQueryMapper get() {
        return new CustomProcessInstanceQueryMapper();
    }

    @Override
    public List<ProcessInstanceDesc> map(Object result) {
        if (result instanceof DataSet) {
            DataSet dataSetResult = (DataSet) result;
            List<ProcessInstanceDesc> mappedResult = new ArrayList<ProcessInstanceDesc>();
            
            if (dataSetResult != null) {
                
                for (int i = 0; i < dataSetResult.getRowCount(); i++) {
                    ProcessInstanceDesc pi = buildInstance(dataSetResult, i);
                    mappedResult.add(pi);
                
                }
            }
            
            return mappedResult;
        }
        
        throw new IllegalArgumentException("Unsupported result for mapping " + result);
    }
    
    protected ProcessInstanceDesc buildInstance(DataSet dataSetResult, int index) {
        ProcessInstanceDesc pi = new org.jbpm.data.ProcessInstanceDesc(
                getColumnLongValue(dataSetResult, COLUMN_PROCESSINSTANCEID, index),
                getColumnStringValue(dataSetResult, COLUMN_PROCESSID, index),
                getColumnStringValue(dataSetResult, COLUMN_PROCESSNAME, index),
                getColumnStringValue(dataSetResult, COLUMN_PROCESSVERSION, index),
                getColumnIntValue(dataSetResult, COLUMN_STATUS, index),
                getColumnStringValue(dataSetResult, COLUMN_EXTERNALID, index),
                getColumnDateValue(dataSetResult, COLUMN_START, index),
                getColumnStringValue(dataSetResult, COLUMN_IDENTITY, index),
                getColumnStringValue(dataSetResult, COLUMN_PROCESSINSTANCEDESCRIPTION, index),
                getColumnStringValue(dataSetResult, COLUMN_CORRELATIONKEY, index), 
                getColumnLongValue(dataSetResult, COLUMN_PARENTPROCESSINSTANCEID, index),
                getColumnDateValue(dataSetResult, COLUMN_SLA_DUE_DATE, index),
                getColumnIntValue(dataSetResult, COLUMN_SLA_COMPLIANCE, index)
                );
        return pi;
    }

    @Override
    public String getName() {
        return "CustomMapper";
    }

    @Override
    public Class<?> getType() {
        return ProcessInstanceDesc.class;
    }
    
    protected Long getColumnLongValue(DataSet currentDataSet, String columnId, int index){
        DataColumn column = currentDataSet.getColumnById(columnId);
        if (column == null) {
            return null;
        }
        
        Object value = column.getValues().get(index);
        return value != null ? ((Number) value).longValue() : null;
    }

    protected String getColumnStringValue(DataSet currentDataSet, String columnId, int index){
        DataColumn column = currentDataSet.getColumnById(columnId);
        if (column == null) {
            return null;
        }
        
        Object value = column.getValues().get(index);
        return value != null ? value.toString() : null;
    }

    protected Date getColumnDateValue(DataSet currentDataSet,String columnId, int index){
        DataColumn column = currentDataSet.getColumnById(columnId);
        if (column == null) {
            return null;
        }
        try{
            return (Date) column.getValues().get( index );
        } catch ( Exception e ){

        }
        return null;
    }

    protected int getColumnIntValue(DataSet currentDataSet,String columnId, int index){
        DataColumn column = currentDataSet.getColumnById( columnId );
        if (column == null) {
            return -1;
        }
        
        Object value = column.getValues().get(index);
        return value != null ? ((Number) value).intValue() : -1;
    }

    @Override
    public QueryResultMapper<List<ProcessInstanceDesc>> forColumnMapping(Map<String, String> columnMapping) {
        return new CustomProcessInstanceQueryMapper();
    }
}
