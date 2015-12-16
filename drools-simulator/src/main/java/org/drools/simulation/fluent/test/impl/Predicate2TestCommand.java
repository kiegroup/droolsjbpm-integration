/*
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.simulation.fluent.test.impl;

import org.drools.core.command.impl.GenericCommand;
import org.drools.simulation.fluent.test.CheckableFluent;
import org.kie.internal.command.Context;

import static org.junit.Assert.fail;

public class Predicate2TestCommand<A, B> implements GenericCommand<Void> {

    private final String var1;
    private final String var2;

    private final CheckableFluent.Predicate2<A, B> predicate;

    private final String reason;

    public Predicate2TestCommand( String var1, String var2, CheckableFluent.Predicate2<A, B> predicate ) {
        this(var1, var2, predicate, "");
    }

    public Predicate2TestCommand( String var1, String var2, CheckableFluent.Predicate2<A, B> predicate, String reason ) {
        this.var1 = var1;
        this.var2 = var2;
        this.predicate = predicate;
        this.reason = reason;
    }

    @Override
    public Void execute( Context context ) {
        boolean result = predicate.test( (A) context.get( var1 ), (B) context.get( var2 ) );
        if (!result) {
            fail(reason);
        }
        return null;
    }
}
