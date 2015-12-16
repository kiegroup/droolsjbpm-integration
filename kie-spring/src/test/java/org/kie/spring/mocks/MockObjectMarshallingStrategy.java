/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.kie.spring.mocks;

import org.kie.api.marshalling.ObjectMarshallingStrategy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MockObjectMarshallingStrategy implements ObjectMarshallingStrategy {

    public boolean accept(Object object) {
        return false;
    }

    public void write(ObjectOutputStream os, Object object) throws IOException {

    }

    public Object read(ObjectInputStream os) throws IOException, ClassNotFoundException {
        return null;
    }

    public byte[] marshal(Context context, ObjectOutputStream os, Object object) throws IOException {
        return new byte[0];
    }

    public Object unmarshal(Context context, ObjectInputStream is, byte[] object, ClassLoader classloader) throws IOException, ClassNotFoundException {
        return null;
    }

    public Context createContext() {
        return null;
    }
}
