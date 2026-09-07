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

import java.util.Iterator;
import java.util.Properties;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook into Hibernate metadata building to ensure ID generators match the target database.
 * On MySQL/MariaDB (which use native IDENTITY columns without sequences), intercepts entity ID generators
 * and adjusts named sequences so Hibernate does not look for sequence emulation tables.
 *
 * Activated via the {@code hibernate.metadata_builder_contributor} property in {@code persistence.xml}
 * or via Hibernate {@link MetadataContributor} SPI.
 */
public class KieServerMetadataBuilderContributor implements MetadataBuilderContributor, MetadataContributor {

    private static final Logger logger = LoggerFactory.getLogger(KieServerMetadataBuilderContributor.class);

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        // MetadataContributor interface handles metadata adjustments in Hibernate 6.6+
    }

    @Override
    public void contribute(InFlightMetadataCollector metadataCollector, IndexView jandexIndex) {
        try {
            Database database = metadataCollector.getDatabase();
            Dialect dialect = database.getDialect();

            if (isMySQL(dialect)) {
                logger.info("Adjusting identifier generators for MySQL/MariaDB dialect: {}", dialect.getClass().getName());

                // Remove sequence definitions on MySQL/MariaDB so no emulation tables are generated/queried
                for (Namespace namespace : database.getNamespaces()) {
                    Iterator<Sequence> seqIter = namespace.getSequences().iterator();
                    while (seqIter.hasNext()) {
                        Sequence seq = seqIter.next();
                        logger.debug("Removing sequence definition on MySQL: {}", seq.getName());
                        seqIter.remove();
                    }
                }

                // Ensure entity identifier generators on MySQL use identity/native strategy
                for (PersistentClass persistentClass : metadataCollector.getEntityBindings()) {
                    if (persistentClass instanceof RootClass) {
                        RootClass rootClass = (RootClass) persistentClass;
                        if (rootClass.getIdentifier() instanceof SimpleValue) {
                            SimpleValue idValue = (SimpleValue) rootClass.getIdentifier();
                            String strategy = idValue.getIdentifierGeneratorStrategy();
                            if ("sequence".equalsIgnoreCase(strategy)
                                    || "org.hibernate.id.enhanced.SequenceStyleGenerator".equalsIgnoreCase(strategy)
                                    || "native".equalsIgnoreCase(strategy)
                                    || "auto".equalsIgnoreCase(strategy)) {
                                logger.debug("Setting identity generator for entity: {}", rootClass.getEntityName());
                                idValue.setIdentifierGeneratorStrategy("identity");
                                Properties params = idValue.getIdentifierGeneratorProperties();
                                if (params == null) {
                                    params = new Properties();
                                    idValue.setIdentifierGeneratorProperties(params);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not adjust identifier generators for MySQL: {}", e.getMessage(), e);
        }
    }

    private boolean isMySQL(Dialect dialect) {
        if (dialect == null) {
            return false;
        }
        String name = dialect.getClass().getName().toLowerCase();
        return name.contains("mysql") || name.contains("mariadb");
    }
}
