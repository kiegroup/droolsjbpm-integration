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

package org.drools.android;

import org.drools.core.rule.JavaDialectRuntimeData;
import org.kie.internal.utils.FastClassLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.drools.core.util.ClassUtils.convertClassToResourcePath;

/**
 * This is an Internal Drools Class
 */
public class DexPackageClassLoader extends MultiDexClassLoader implements FastClassLoader {

    protected JavaDialectRuntimeData store;

    private Set<String> existingPackages = new ConcurrentSkipListSet<String>();

    public DexPackageClassLoader(JavaDialectRuntimeData store,
                                 ClassLoader rootClassLoader) {
        super(rootClassLoader);
        this.store = store;
    }

    public Class<?> loadClass(final String name,
                              final boolean resolve) throws ClassNotFoundException {
        Class<?> cls = fastFindClass(name);

        if (cls == null) {
            ClassLoader parent = getParent();
            cls = parent.loadClass(name);
        }

        if (cls == null) {
            throw new ClassNotFoundException("Unable to load class: " + name);
        }

        return cls;
    }

    public Class<?> fastFindClass(final String name) {
        Class<?> cls = findLoadedClass(name);

        if (cls == null) {
            final byte[] clazzBytes = this.store.read(convertClassToResourcePath(name));
            if (clazzBytes != null) {
                String pkgName = name.substring(0,
                        name.lastIndexOf('.'));

                if (!existingPackages.contains(pkgName)) {
                    synchronized (this) {
                        if (getPackage(pkgName) == null) {
                            definePackage(pkgName,
                                    "", "", "", "", "", "",
                                    null);
                        }
                        existingPackages.add(pkgName);
                    }
                }
                cls = super.defineClass(name, clazzBytes);
            }

            if (cls != null) {
                resolveClass(cls);
            }
        }

        return cls;
    }

    public InputStream getResourceAsStream(final String name) {
        final byte[] clsBytes = this.store.read(name);
        if (clsBytes != null) {
            return new ByteArrayInputStream(clsBytes);
        }
        return null;
    }

    public URL getResource(String name) {
        return null;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        return new Enumeration<URL>() {

            public boolean hasMoreElements() {
                return false;
            }

            public URL nextElement() {
                throw new NoSuchElementException();
            }
        };
    }

}