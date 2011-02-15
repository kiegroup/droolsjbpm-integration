/**
 * Copyright 2010 JBoss Inc
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

package org.drools.runtime.pipeline.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.Callable;
import org.drools.runtime.pipeline.PipelineFactory;

public class CallableTest {
    @Test
    public void testCallable() {
        MockClass mock = new MockClass();
        Callable callable = new CallableImpl();
        Action action = PipelineFactory.newMvelAction( "this.set = true" );
        callable.setReceiver( action );
        action.setReceiver( callable );
        assertFalse( mock.isSet() );
        callable.call( mock,
                       new BasePipelineContext( Thread.currentThread().getContextClassLoader() ) );
        assertTrue( mock.isSet() );
    }

    public static class MockClass {
        private boolean set;

        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }

    }
}
