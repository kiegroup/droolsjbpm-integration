/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.drools.android.robolectric;

import org.drools.android.MultiDexClassLoader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.security.ProtectionDomain;

/**
 * {@link MultiDexClassLoader} which loads classes normally,
 * not converting to dex first
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = MultiDexClassLoader.class, callThroughByDefault = false, inheritImplementationMethods = true)
public class ShadowMultiDexClassLoader extends ShadowDexClassLoader {

    @Implementation
    public void __constructor__(ClassLoader parent) {
        System.out.println("CREATING MULTIDEX SHADOW __constructor__!!!!!!!!!!!!!!!!!!");
        super.__constructor__(null, null, null, parent);
    }

    @Implementation
    public Class defineClass(String name, byte[] bytes) {
        System.out.println(String.format("Shadow defineClass(%s, %s)", name, new String(bytes)));
        return super.defineClass(name, bytes, 0, bytes.length);
    }

    @Implementation
    public Class<?> defineClass(final String name,
                                final byte[] bytes,
                                final ProtectionDomain domain) {
        return defineClass(name, bytes);
    }

    @Implementation
    public Class defineClassX(String className, byte[] b, int start, int len) {
        System.out.println(String.format("Shadow defineClass(%s, %s)", className, new String(b)));
        return super.defineClass(className, b, start, len);
    }
}
