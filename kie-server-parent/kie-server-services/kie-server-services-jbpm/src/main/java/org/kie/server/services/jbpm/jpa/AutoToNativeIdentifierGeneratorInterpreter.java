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

import java.util.Properties;
import java.util.function.BiConsumer;

import jakarta.persistence.GenerationType;

import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.internal.IdentityGenerationTypeStrategy;
import org.hibernate.id.factory.spi.GenerationTypeStrategy;
import org.hibernate.id.factory.spi.GenerationTypeStrategyRegistration;
import org.hibernate.id.factory.spi.GeneratorDefinitionResolver;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.java.JavaType;

/**
 * Restores Hibernate 5 behaviour for {@code GenerationType.AUTO} on dialects
 * that support identity columns but not native sequences (e.g. MySQL, older
 * SQL Server). For sequence-capable dialects (PostgreSQL, Oracle, H2, DB2,
 * SQL Server 2012+) the default strategy is left unchanged.
 *
 * <p>Registered via
 * {@code META-INF/services/org.hibernate.id.factory.spi.GenerationTypeStrategyRegistration}.
 */
public class AutoToNativeIdentifierGeneratorInterpreter implements GenerationTypeStrategyRegistration {

    @Override
    public void registerStrategies(BiConsumer<GenerationType, GenerationTypeStrategy> registry,
                                   ServiceRegistry serviceRegistry) {
        Dialect dialect = serviceRegistry.requireService(
                org.hibernate.engine.jdbc.env.spi.JdbcEnvironment.class).getDialect();

        if (!dialect.getSequenceSupport().supportsSequences()) {
            // Dialect has no native sequence support (MySQL, older SQL Server):
            // redirect AUTO to identity/auto-increment, matching the AUTO_INCREMENT
            // and IDENTITY columns in the corresponding DDL scripts.
            registry.accept(GenerationType.AUTO, IdentityGenerationTypeStrategy.INSTANCE);
        }
        // On sequence-capable dialects: do nothing, preserving SequenceStyleGenerator behaviour.
    }
}
