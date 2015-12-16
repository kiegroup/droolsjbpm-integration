/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.drools.benchmark.model;

import java.math.BigInteger;

public class Fibonacci {
    private int sequence;

    private BigInteger value;

    public Fibonacci() { }

    public Fibonacci(final int sequence) {
        this.sequence = sequence;
        this.value = new BigInteger("-1");
    }

    public int getSequence() {
        return this.sequence;
    }

    public void setValue(final BigInteger value) {
        this.value = value;
    }
    public BigInteger getValue() {
        return this.value;
    }

    public String toString() {
        return "Fibonacci(" + this.sequence + "/" + this.value + ")";
    }
}
