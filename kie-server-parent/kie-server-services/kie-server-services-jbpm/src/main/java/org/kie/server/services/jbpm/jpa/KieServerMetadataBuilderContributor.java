/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.jpa;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.boot.spi.MetadataBuilderImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers {@link AutoToNativeIdentifierGeneratorInterpreter} with the
 * Hibernate metadata builder so that {@code GenerationType.AUTO} is resolved
 * to the correct strategy for each database without requiring schema changes:
 * <ul>
 *   <li>Dialects without native sequence support (MySQL, older SQL Server):
 *       {@code AUTO} maps to {@code identity}, matching the {@code AUTO_INCREMENT}
 *       / {@code IDENTITY} columns in those DDL scripts.</li>
 *   <li>Dialects with native sequence support (PostgreSQL, Oracle, H2, DB2,
 *       SQL Server 2012+): default behaviour is preserved and
 *       {@code SequenceStyleGenerator} uses the existing {@code CREATE SEQUENCE}
 *       objects.</li>
 * </ul>
 *
 * Activated via the {@code hibernate.metadata_builder_contributor} property
 * in {@code persistence.xml}.
 */
public class KieServerMetadataBuilderContributor implements MetadataBuilderContributor {

    private static final Logger logger = LoggerFactory.getLogger(KieServerMetadataBuilderContributor.class);

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        ServiceRegistry serviceRegistry = ((MetadataBuilderImplementor) metadataBuilder)
                .getBootstrapContext()
                .getServiceRegistry();

        // Resolve the dialect class from the setting already in the configuration map.
        // We use the ClassLoaderService so the WAR classloader is used, then instantiate
        // a temporary dialect instance only to check supportsSequences().
        String dialectClassName = (String) serviceRegistry
                .getService(org.hibernate.engine.config.spi.ConfigurationService.class)
                .getSettings()
                .get("hibernate.dialect");

        if (dialectClassName == null) {
            logger.warn("KieServerMetadataBuilderContributor: hibernate.dialect not set, skipping AUTO generator fix");
            return;
        }

        try {
            Class<?> dialectClass = serviceRegistry
                    .getService(ClassLoaderService.class)
                    .classForName(dialectClassName);
            Dialect dialect = (Dialect) dialectClass.getDeclaredConstructor().newInstance();

            logger.info("KieServerMetadataBuilderContributor: applying AUTO id generator fix for dialect {}", dialectClassName);
            metadataBuilder.applyIdGenerationTypeInterpreter(
                    new AutoToNativeIdentifierGeneratorInterpreter(dialect));
        } catch (Exception e) {
            logger.error("KieServerMetadataBuilderContributor: failed to instantiate dialect {}, skipping AUTO generator fix", dialectClassName, e);
        }
    }
}
