package org.drools.core.common;

import org.drools.android.MultiDexClassLoader;

/**
 * @author kedzie
 */
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
