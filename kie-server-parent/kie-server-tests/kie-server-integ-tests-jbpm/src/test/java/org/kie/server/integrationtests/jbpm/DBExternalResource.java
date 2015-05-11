package org.kie.server.integrationtests.jbpm;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.junit.rules.ExternalResource;
import org.kie.server.integrationtests.shared.KieServerBaseIntegrationTest;

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
