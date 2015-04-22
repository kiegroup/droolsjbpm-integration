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

package org.drools.android;

import android.content.Context;
import org.drools.core.rule.builder.dialect.asm.ClassLevel;
import org.mvel2.optimizers.OptimizerFactory;
import org.mvel2.optimizers.impl.asm.ASMAccessorOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Holds android context. Must be called at the start of the Android application before drools is used.
 */
@SuppressWarnings("PackageAccessibility")
public class DroolsAndroidContext {
    private static final Logger log = LoggerFactory.getLogger(DroolsAndroidContext.class);

    private static Context context;
    private static boolean reuseClassFiles = true;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context ctx) {
        context = ctx;
        cacheDir = new File(context.getCacheDir(), "drools");
        dexDir = new File(cacheDir, "dex");
        if (!isReuseClassFiles()) deleteDir(dexDir);
        dexDir.mkdirs();
        optimizedDir = new File(cacheDir, "optimized");
        optimizedDir.mkdirs();

        System.setProperty(ClassLevel.JAVA_LANG_LEVEL_PROPERTY, "1.6");
        System.setProperty("java.version", "1.6");
        System.setProperty("mvel.java.version", "1.6");

        ASMAccessorOptimizer.setMVELClassLoader(
                new MultiDexClassLoader((ClassLoader) ASMAccessorOptimizer.getMVELClassLoader()));
        OptimizerFactory.setDefaultOptimizer("ASM");
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private static File cacheDir;
    private static File dexDir;
    private static File optimizedDir;

    public static boolean isReuseClassFiles() {
        return reuseClassFiles;
    }

    public static void setReuseClassFiles(boolean reuse) {
        reuseClassFiles = reuse;
    }

    public static File getCacheDir() {
        return cacheDir;
    }

    public static File getDexDir() {
        return dexDir;
    }

    public static File getOptimizedDir() {
        return optimizedDir;
    }
}
