package org.drools.android.robolectric;

import dalvik.system.BaseDexClassLoader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * {@link BaseDexClassLoader} which loads classes normally,
 * not converting to dex first
 */
@Implements(value=BaseDexClassLoader.class, looseSignatures = true, callThroughByDefault = false, inheritImplementationMethods = true)
public class ShadowBaseDexClassLoader extends ClassLoader{

    @Implementation
    public void __constructor__(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
        System.out.println("CREATING Base DEX SHADOW __constructor__!!!!!!!!!!!!!!!!!!");
    }

    @Implementation
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Implementation
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Implementation
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Implementation
    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Implementation
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    @Implementation
    @Override
    protected URL findResource(String name) {
        return super.findResource(name);
    }

    @Implementation
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    @Implementation
    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    @Implementation
    @Override
    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
    }
}
