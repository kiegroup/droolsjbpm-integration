/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.prometheus;

import java.util.List;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.DataSetDefRegistryListener;
import org.dashbuilder.dataset.def.DataSetPostProcessor;
import org.dashbuilder.dataset.def.DataSetPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.prometheus.PrometheusMetrics.millisToSeconds;

public class PrometheusDataSetListener implements DataSetDefRegistryListener,
                                                  DataSetPreprocessor,
                                                  DataSetPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusDataSetListener.class);
    private static final String PROMETHEUS_META = "prometheus_start";
    
    protected static final Gauge numberOfRegisteredDataSet = Gauge.build()
            .name("kie_server_data_set_registered_total")
            .help("Kie Server Data Set Registered")
            .labelNames("name", "uuid")
            .register();

    protected static final Gauge numberOfRunningDataSetLookups = Gauge.build()
            .name("kie_server_data_set_lookups_total")
            .help("Kie Server Data Set Running Lookups")
            .labelNames("uuid")
            .register();

    protected static final Summary dataSetExecutionTime = Summary.build()
            .name("kie_server_data_set_execution_time_seconds")
            .help("Kie Server Data Set Execution Time")
            .labelNames("uuid")
            .register();

    protected static final Counter dataSetExecution = Counter.build()
            .name("kie_server_data_set_execution_total")
            .help("Kie Server Data Set Execution")
            .labelNames("uuid")
            .register();
    
    private DataSetDefRegistry dataSetDefRegistry;

    public PrometheusDataSetListener(DataSetDefRegistry dataSetDefRegistry) {
        this.dataSetDefRegistry = dataSetDefRegistry;
    }

    protected void init() {
        final List<DataSetDef> dataSetDefs = dataSetDefRegistry.getDataSetDefs(false);
        dataSetDefs.forEach(dataSetDef -> onDataSetDefRegistered(dataSetDef));
        LOGGER.debug("Loaded {} data sets", dataSetDefs.size());
    }

    @Override
    public void onDataSetDefStale(DataSetDef dataSetDef) {

    }

    @Override
    public void onDataSetDefModified(DataSetDef oldDef, DataSetDef newDef) {
        final List<DataSetPreprocessor> dataSetDefPreProcessors = dataSetDefRegistry.getDataSetDefPreProcessors(newDef.getUUID());
        if (dataSetDefPreProcessors == null || dataSetDefPreProcessors.stream().anyMatch(p -> p instanceof PrometheusDataSetListener) == false) {
            dataSetDefRegistry.registerPreprocessor(newDef.getUUID(), this);
            LOGGER.debug("Registered PreProcessor for data set: {}", newDef.getUUID());
        }

        final List<DataSetPostProcessor> dataSetDefPostProcessors = dataSetDefRegistry.getDataSetDefPostProcessors(newDef.getUUID());
        if (dataSetDefPostProcessors == null || dataSetDefPostProcessors.stream().anyMatch(p -> p instanceof PrometheusDataSetListener) == false) {
            dataSetDefRegistry.registerPostProcessor(newDef.getUUID(), this);
            LOGGER.debug("Registered PostProcessor for data set: {}", newDef.getUUID());
        }
    }

    @Override
    public void onDataSetDefRegistered(DataSetDef dataSetDef) {
        LOGGER.debug("On Data Set Def Registered: {}", dataSetDef.getUUID());
        numberOfRegisteredDataSet.labels(dataSetDef.getName(), dataSetDef.getUUID()).inc();
        dataSetDefRegistry.registerPreprocessor(dataSetDef.getUUID(), this);
        dataSetDefRegistry.registerPostProcessor(dataSetDef.getUUID(), this);
        LOGGER.debug("Registered Pre and Post processors for data set: {}", dataSetDef.getUUID());
    }

    @Override
    public void onDataSetDefRemoved(DataSetDef dataSetDef) {
        LOGGER.debug("On Data Set Def Removed: {}", dataSetDef);
        numberOfRegisteredDataSet.labels(dataSetDef.getName(), dataSetDef.getUUID()).dec();
    }

    @Override
    public void postProcess(DataSetLookup lookup, DataSet dataSet) {
        LOGGER.debug("On Data Set Post Process: {}", dataSet.getUUID());
        numberOfRunningDataSetLookups.labels(dataSet.getUUID()).dec();
        dataSetExecution.labels(dataSet.getUUID()).inc();
        final Long start = (Long) lookup.getMetadata(PROMETHEUS_META);
        if (start != null) {
            final double duration = millisToSeconds(System.currentTimeMillis() - start);
            LOGGER.debug("Data Set query duration: {}s", duration);
            dataSetExecutionTime.labels(lookup.getDataSetUUID()).observe(duration);
        }
    }

    @Override
    public void preprocess(DataSetLookup lookup) {
        LOGGER.debug("On Data Set Pre Process: {}", lookup.getDataSetUUID());
        numberOfRunningDataSetLookups.labels(lookup.getDataSetUUID()).inc();
        lookup.setMetadata(PROMETHEUS_META, System.currentTimeMillis());
    }
}
