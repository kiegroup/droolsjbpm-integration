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

package org.drools.android.robolectric;

import dalvik.system.DexClassLoader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;

/**
 * {@link DexClassLoader} which loads classes normally,
 * not converting to dex first
 */
@Implements(value = DexClassLoader.class, looseSignatures = true, callThroughByDefault = false, inheritImplementationMethods = true)
public class ShadowDexClassLoader extends ShadowBaseDexClassLoader {

    @Implementation
    public void __constructor__(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
        System.out.println("CREATING DEX SHADOW __constructor__!!!!!!!!!!!!!!!!!!");
        super.__constructor__(dexPath, optimizedDirectory, libraryPath, parent);
    }
}
