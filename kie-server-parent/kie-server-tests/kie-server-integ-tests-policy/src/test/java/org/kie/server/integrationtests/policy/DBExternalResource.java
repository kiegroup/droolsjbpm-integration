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

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.junit.rules.ExternalResource;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;

public class DBExternalResource extends ExternalResource {
    PoolingDataSource pds;

    @Override
    protected void after() {

        if (pds != null) {
            pds.close();
        }
    };

    @Override
    protected void before() throws Throwable {

        KieServerBaseIntegrationTest.cleanupSingletonSessionId();

        pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(50);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password", "");
        pds.getDriverProperties().put("url", "jdbc:h2:mem:jbpm-db;MVCC=true");
        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        pds.init();
    };
};
