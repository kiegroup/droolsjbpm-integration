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
@Implements(value=MultiDexClassLoader.class, callThroughByDefault = false, inheritImplementationMethods = true)
public class ShadowMultiDexClassLoader extends ShadowDexClassLoader {

    @Implementation
    public void __constructor__(ClassLoader parent) {
        System.out.println("CREATING MULTIDEX SHADOW __constructor__!!!!!!!!!!!!!!!!!!");
        super.__constructor__(null, null, null, parent);
    }

    @Implementation
    public Class defineClass(String name, byte[] bytes) {
        System.out.println(String.format("Shadow defineClass(%s, %s)", name, bytes));
        return super.defineClass(name, bytes, 0, bytes.length);
    }

    @Implementation
    public Class< ? > defineClass(final String name,
                                  final byte[] bytes,
                                  final ProtectionDomain domain) {
        return defineClass(name, bytes);
    }

    @Implementation
    public Class defineClassX(String className, byte[] b, int start, int len) {
        System.out.println(String.format("Shadow defineClass(%s, %s)", className, b));
        return super.defineClass(className, b, start, len);
    }
}
