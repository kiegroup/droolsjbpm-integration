/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.policy;

import java.util.Properties;

import org.junit.rules.ExternalResource;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.kie.test.util.db.DataSourceFactory;
import org.kie.test.util.db.PoolingDataSourceWrapper;

public class DBExternalResource extends ExternalResource {
    PoolingDataSourceWrapper pds;

    @Override
    protected void after() {

        if (pds != null) {
            pds.close();
        }
    };

    @Override
    protected void before() throws Throwable {

        KieServerBaseIntegrationTest.cleanupSingletonSessionId();
        if (TestConfig.isLocalServer()) {
            Properties driverProperties = new Properties();
            driverProperties.setProperty("user", "sa");
            driverProperties.setProperty("password", "");
            driverProperties.setProperty("url", "jdbc:h2:mem:jbpm-db;MVCC=true");
            driverProperties.setProperty("className", "org.h2.jdbcx.JdbcDataSource");
            driverProperties.setProperty("driverClassName", "org.h2.Driver");

            pds = DataSourceFactory.setupPoolingDataSource("jdbc/jbpm-ds", driverProperties);
        }
    };
};
