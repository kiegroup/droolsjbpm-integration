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

import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.TableGenerator;

import org.hibernate.boot.model.IdGeneratorStrategyInterpreter;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.dialect.Dialect;

/**
 * Restores Hibernate 5 behaviour for {@code GenerationType.AUTO} on dialects
 * that support identity columns but not native sequences (e.g. MySQL,
 * SQL Server without sequences). For sequence-capable dialects (PostgreSQL,
 * Oracle, H2, DB2, SQL Server 2012+) returns {@code null} / delegates to the
 * default interpreter so {@code SequenceStyleGenerator} uses the existing
 * {@code CREATE SEQUENCE} objects in those schemas.
 *
 * <p>The key override is {@link #interpretSequenceGenerator}: on non-sequence
 * dialects this forces the named generator definition's strategy to
 * {@code "identity"} instead of {@code SequenceStyleGenerator}. Without this,
 * the named generator definition (built from the {@code @SequenceGenerator}
 * annotation) would overwrite the strategy returned by
 * {@link #determineGeneratorName} in {@code GeneratorBinder.makeIdGenerator}.
 *
 * <p>This restores compatibility with all existing jBPM DDL scripts without
 * requiring any schema changes.
 */
public class AutoToNativeIdentifierGeneratorInterpreter implements IdGeneratorStrategyInterpreter {

    private final Dialect dialect;
    private final boolean useIdentity;

    public AutoToNativeIdentifierGeneratorInterpreter(Dialect dialect) {
        this.dialect = dialect;
        this.useIdentity = !dialect.getSequenceSupport().supportsSequences();
    }

    @Override
    public String determineGeneratorName(GenerationType generationType,
                                         GeneratorNameDeterminationContext context) {
        if (generationType == GenerationType.AUTO && useIdentity) {
            // Dialect has no native sequence support (MySQL, older SQL Server):
            // redirect AUTO to identity/auto-increment, matching the AUTO_INCREMENT
            // and IDENTITY columns in the corresponding DDL scripts.
            return "identity";
        }
        // Return null to let the default interpreter handle all other cases.
        return null;
    }

    @Override
    public void interpretTableGenerator(TableGenerator tableGenerator,
                                         IdentifierGeneratorDefinition.Builder builder) {
        // no override needed
    }

    /**
     * On dialects without native sequence support, force the named generator
     * definition strategy to {@code "identity"} so that the AUTO_INCREMENT /
     * IDENTITY column is used. This is necessary because
     * {@code GeneratorBinder.makeIdGenerator} looks up the named
     * {@code IdentifierGeneratorDefinition} (built here from the
     * {@code @SequenceGenerator} annotation) and uses <em>its</em> strategy,
     * overriding the value returned by {@link #determineGeneratorName}.
     *
     * On sequence-capable dialects this method does nothing, preserving the
     * default {@code SequenceStyleGenerator} behaviour.
     */
    @Override
    public void interpretSequenceGenerator(SequenceGenerator sequenceGenerator,
                                            IdentifierGeneratorDefinition.Builder builder) {
        if (useIdentity) {
            builder.setName(sequenceGenerator.name());
            builder.setStrategy("identity");
        }
        // On sequence-capable dialects: do nothing, fall through to the default
        // interpreter which sets SequenceStyleGenerator + sequence_name params.
    }
}
