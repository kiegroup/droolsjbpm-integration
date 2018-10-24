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

import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.kie.processmigration.jpa.converter.CryptoConverter;

public abstract class PersistenceTest {

    protected EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("migration-test");
    protected EntityManager entityManager = entityManagerFactory.createEntityManager();

    @BeforeClass
    public static void setEnv() {
        System.setProperty(CryptoConverter.PASSWORD_PROPERTY, "gUkXp2s5v8x/A?D(");
    }

    @AfterClass
    public static void unsetEnv() {
        System.clearProperty(CryptoConverter.PASSWORD_PROPERTY);
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected Function<InjectionPoint, Object> getPCFactory() {
        return ip -> entityManager;
    }

    protected Function<InjectionPoint, Object> getPUFactory() {
        return ip -> entityManagerFactory;
    }
}
