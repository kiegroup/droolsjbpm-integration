/*
 * Copyright 2015 JBoss by Red Hat
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

package org.drools.core.common;

import org.drools.android.MultiDexClassLoader;

public class DexInternalTypesClassLoader extends MultiDexClassLoader {

    private final ProjectClassLoader projectClassLoader;

    public DexInternalTypesClassLoader(ProjectClassLoader projectClassLoader) {
        super(null);
        this.projectClassLoader = projectClassLoader;
    }

    public Class<?> defineClass(String name, byte[] bytecode) {
        int lastDot = name.lastIndexOf( '.' );
        if (lastDot > 0) {
            String pkgName = name.substring( 0, lastDot );
            if (getPackage( pkgName ) == null) {
                definePackage( pkgName, "", "", "", "", "", "", null );
            }
        }
        return super.defineClass(name, bytecode);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return loadType(name, resolve);
        } catch (ClassNotFoundException cnfe) {
            synchronized(projectClassLoader) {
                try {
                    return projectClassLoader.internalLoadClass(name, resolve);
                } catch (ClassNotFoundException cnfe2) {
                    return projectClassLoader.tryDefineType(name, cnfe);
                }
            }
        }
    }

    private Class<?> loadType(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }
}
