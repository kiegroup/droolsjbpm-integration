/*
 * Copyright 2005 JBoss Inc
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

import org.drools.simulation.fluent.test.CheckableFluent;

public class CheckableFactory<P> implements CheckableFluent<P> {

    private final AbstractTestableFluent<P> testable;
    private final String var1;

    public CheckableFactory( AbstractTestableFluent<P> testable, String var1 ) {
        this.testable = testable;
        this.var1 = var1;
    }

    @Override
    public <A> BoundCheckable1<P, A> as( Class<A> a ) {
        return new BoundCheckable1Impl( testable, var1 );
    }

    public static class BoundCheckable1Impl<P, A> implements BoundCheckable1<P, A> {
        private final AbstractTestableFluent<P> testable;
        private final String var1;

        public BoundCheckable1Impl( AbstractTestableFluent<P> testable, String var1 ) {
            this.testable = testable;
            this.var1 = var1;
        }

        public P test( Predicate1<A> predicate ) {
            testable.addCommand( new Predicate1TestCommand( var1, predicate ) );
            return (P)testable;
        }

        public P test( Predicate1<A> predicate, String reason ) {
            testable.addCommand( new Predicate1TestCommand( var1, predicate, reason ) );
            return (P)testable;
        }

        public UnboundCheckable1<P, A> given(String name) {
            return new UnboundCheckable1Impl<P, A>( testable, var1, name );
        }
    }

    public static class UnboundCheckable1Impl<P, A> implements UnboundCheckable1<P, A> {
        private final AbstractTestableFluent<P> testable;
        private final String var1;
        private final String var2;

        public UnboundCheckable1Impl( AbstractTestableFluent<P> testable, String var1, String var2 ) {
            this.testable = testable;
            this.var1 = var1;
            this.var2 = var2;
        }

        public <B> BoundCheckable2<P, A, B> as(Class<B> b) {
            return new BoundCheckable2Impl(testable, var1, var2);
        }
    }

    public static class BoundCheckable2Impl<P, A, B> implements BoundCheckable2<P, A, B> {
        private final AbstractTestableFluent<P> testable;
        private final String var1;
        private final String var2;

        public BoundCheckable2Impl( AbstractTestableFluent<P> testable, String var1, String var2 ) {
            this.testable = testable;
            this.var1 = var1;
            this.var2 = var2;
        }

        public P test( Predicate2<A, B> predicate ) {
            testable.addCommand( new Predicate2TestCommand( var1, var2, predicate ) );
            return (P)testable;
        }

        public P test( Predicate2<A, B> predicate, String reason ) {
            testable.addCommand( new Predicate2TestCommand( var1, var2, predicate, reason ) );
            return (P)testable;
        }
    }
}
