package org.kie.server.integrationtests.jbpm;

import java.io.File;
import java.io.FilenameFilter;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public class JbpmKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {


    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();


    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    protected static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {

            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {

                new File(tempDir, file).delete();
            }
        }
    }

    static class DBExternalResource extends ExternalResource {
        PoolingDataSource pds;

        @Override
        protected void after() {
            if (pds != null) {
                pds.close();
            }
        };

        @Override
        protected void before() throws Throwable {
            cleanupSingletonSessionId();

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
}
