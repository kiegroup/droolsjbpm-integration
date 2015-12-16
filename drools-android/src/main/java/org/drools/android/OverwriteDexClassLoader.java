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

import com.android.dex.DexFormat;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.ClassDefItem;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Classloader which has a single dex file with all classes inside it.
 * It is overwritten each time a new class is defined.
 */
public class OverwriteDexClassLoader extends DexClassLoader {
    private static final Logger log = LoggerFactory.getLogger(OverwriteDexClassLoader.class);

    final DexOptions dex_options = new DexOptions();
    final CfOptions cf_options = new CfOptions();
    private com.android.dx.dex.file.DexFile dexFile;
    private List<ClassDefItem> items = new LinkedList<ClassDefItem>();
    private File file;
    private static Field pathListField;
    private static Class dexPathListClazz;
    private static Constructor dexPathListConstructor;

    static {
        try {
            dexPathListClazz = Class.forName("dalvik.system.DexPathList");
            dexPathListConstructor = dexPathListClazz.getConstructor(ClassLoader.class, String.class, String.class, File.class);
            pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
            pathListField.setAccessible(true);
        } catch (Exception e) {
            log.error("Reflection error", e);
        }
    }

    public OverwriteDexClassLoader(String name, ClassLoader parent) {
        super(new File(DroolsAndroidContext.getDexDir(), name + ".dex").getAbsolutePath(),
                DroolsAndroidContext.getOptimizedDir().getAbsolutePath(),
                DroolsAndroidContext.getContext().getApplicationInfo().nativeLibraryDir,
                parent != null ? parent : DroolsAndroidContext.getContext().getClassLoader());
        file = new File(DroolsAndroidContext.getDexDir(), name+".dex");
        dex_options.targetApiLevel = DexFormat.API_NO_EXTENDED_OPCODES;
        cf_options.optimize = true;
    }

    protected void setName(String name) {
        file = new File(DroolsAndroidContext.getDexDir(), name+".dex");
    }

    public Class defineClass(String name, byte[] bytes) {
        log.trace(file.getName() + " classloader - Defining class " + name);
        DirectClassFile cf =
                new DirectClassFile(bytes, name.replace('.', '/') + ".class", cf_options.strictNameCheck);
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.getMagic();
        dexFile = new com.android.dx.dex.file.DexFile(dex_options);
        items.add(CfTranslator.translate(cf, bytes, cf_options, dex_options, dexFile));
        for(ClassDefItem item : items)
            dexFile.add(item);

        FileOutputStream fos = null;
        try {
            if (file.exists())
                file.delete();
            fos = new FileOutputStream(file);
            dexFile.writeTo(fos, null, false);
            pathListField.set(this, dexPathListConstructor.newInstance(this,
                    file.getAbsolutePath(),
                    DroolsAndroidContext.getContext().getApplicationInfo().nativeLibraryDir,
                    DroolsAndroidContext.getOptimizedDir()));
            return findClass(name);
        } catch(Exception e) {
            log.error("error", e);
            throw new RuntimeException(e);
        } finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {}
            }
        }
    }
}