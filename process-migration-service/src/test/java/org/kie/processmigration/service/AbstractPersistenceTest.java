/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.processmigration.service;

import java.util.Properties;
import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.kie.processmigration.persistence.TestEntityManager;
import org.kie.test.util.db.PersistenceUtil;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.mockito.Mockito;

public abstract class AbstractPersistenceTest extends AbstractBeanBasedTest {

    private PoolingDataSourceWrapper ds;
    private EntityManagerFactory emf;

    protected EntityManagerFactory entityManagerFactory = getEntityManagerFactory();

    protected EntityManager entityManager = Mockito.spy(new TestEntityManager(entityManagerFactory));

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected Function<InjectionPoint, Object> getPCFactory() {
        return ip -> entityManager;
    }

    private EntityManagerFactory getEntityManagerFactory() {
        Properties dsProps = PersistenceUtil.getDatasourceProperties();
        ds = PersistenceUtil.setupPoolingDataSource(dsProps, "jdbc/testDS1");
        emf = Persistence.createEntityManagerFactory("org.kie.test.persistence");
        return emf;
    }

    @After
    public void closeResources() {
        if(emf != null) {
            emf.close();
        }
        if(ds != null) {
            ds.close();
        }
    }
}
