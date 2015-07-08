package org.drools.android.robolectric;

import dalvik.system.DexClassLoader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;

/**
 * {@link DexClassLoader} which loads classes normally,
 * not converting to dex first
 */
@Implements(value=DexClassLoader.class, looseSignatures = true, callThroughByDefault = false, inheritImplementationMethods = true)
public class ShadowDexClassLoader extends ShadowBaseDexClassLoader {

    @Implementation
    public void __constructor__(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
        System.out.println("CREATING DEX SHADOW __constructor__!!!!!!!!!!!!!!!!!!");
        super.__constructor__(dexPath, optimizedDirectory, libraryPath, parent);
    }
}
