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

package org.kie.spring.jbpm;

import org.kie.spring.jbpm.tools.IntegrationSpringBase;
import java.io.File;
import java.io.FilenameFilter;

import javax.naming.Context;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.jbpm.test.util.PoolingDataSource;

public abstract class AbstractJbpmSpringTest extends IntegrationSpringBase {

    protected static PoolingDataSource pds;
    protected ClassPathXmlApplicationContext context;

    @BeforeClass
    public static void generalSetup() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jbpm.test.util.CloseSafeMemoryContextFactory");
        System.setProperty("org.osjava.sj.root", "target/test-classes/config");
        System.setProperty("org.osjava.jndi.delimiter", "/");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        pds = setupPoolingDataSource();
    }

    @Before
    public void setup() {
        cleanupSingletonSessionId();
    }

    @After
    public void cleanup() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

    @AfterClass
    public static void generalCleanup() { 
        if (pds != null) {
            pds.close();
        }
        System.clearProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
    }

    protected static PoolingDataSource setupPoolingDataSource() {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("org.h2.jdbcx.JdbcDataSource");
        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password", "");
        pds.getDriverProperties().put("URL", "jdbc:h2:mem:jbpm-db;MVCC=true");
        pds.init();
        return pds;
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
}
