package org.kie.maven.plugin;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.drools.core.phreak.ReactiveObject;

import org.junit.Test;

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;

public class InjectReactiveIntegrationTest extends KieMavenPluginBaseIntegrationTest {

    public InjectReactiveIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        super(builder);
    }

    @Test
    public void testBasicBytecodeInjection() throws Exception {
        File basedir = resources.getBasedir("kjar-4-bytecode-inject");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean", "install");
        result.assertErrorFreeLog();
        
        File classDir = new File(basedir, "target/classes");
        
        System.out.println(classDir);
        
        List<URL> classloadingURLs = new ArrayList<>();
        classloadingURLs.add(classDir.toURI().toURL());
        classloadingURLs.add(new File(BytecodeInjectReactive.classpathFromClass(ReactiveObject.class)).toURI().toURL());
        File libDir = new File(basedir, "target/lib");
        for (File jar : libDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        })) {
            classloadingURLs.add(jar.toURI().toURL());
        }
        
        ClassLoader cl = new URLClassLoader( classloadingURLs.toArray(new URL[]{}), null );
        
        assertTrue( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.Adult") ) );
        assertTrue( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.UsingADependencyClass") ) );
        assertTrue( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.UsingSpecializedList") ) );
        assertTrue( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.TMFile") ) );
        assertTrue( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.TMFileSet") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.ImmutablePojo") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.FieldIsNotListInterface") ) );
    }
    
    @Test
    public void testBasicBytecodeInjectionSelected() throws Exception {
        File basedir = resources.getBasedir("kjar-5-bytecode-inject-selected");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean", "install");
        result.assertErrorFreeLog();
        
        File classDir = new File(basedir, "target/classes");
        
        System.out.println(classDir);
        
        List<URL> classloadingURLs = new ArrayList<>();
        classloadingURLs.add(classDir.toURI().toURL());
        classloadingURLs.add(new File(BytecodeInjectReactive.classpathFromClass(ReactiveObject.class)).toURI().toURL());
        File libDir = new File(basedir, "target/lib");
        for (File jar : libDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        })) {
            classloadingURLs.add(jar.toURI().toURL());
        }
        
        ClassLoader cl = new URLClassLoader( classloadingURLs.toArray(new URL[]{}), null );
        
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.Adult") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.UsingADependencyClass") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.UsingSpecializedList") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.TMFile") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.TMFileSet") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("org.drools.compiler.xpath.tobeinstrumented.model.ImmutablePojo") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("to.instrument.Adult") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("to.instrument.UsingADependencyClass") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("to.instrument.UsingSpecializedList") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("to.instrument.TMFile") ) );
        assertTrue ( looksLikeInstrumentedClass( cl.loadClass("to.instrument.TMFileSet") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.instrument.ImmutablePojo") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.not.instrument.Adult") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.not.instrument.UsingADependencyClass") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.not.instrument.UsingSpecializedList") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.not.instrument.TMFile") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.not.instrument.TMFileSet") ) );
        assertFalse( looksLikeInstrumentedClass( cl.loadClass("to.not.instrument.ImmutablePojo") ) );
    }

    private boolean looksLikeInstrumentedClass(Class<?> personClass) {
        boolean foundReactiveObjectInterface = false;
        for ( Class<?> i : personClass.getInterfaces() ){
            if ( i.getName().equals(ReactiveObject.class.getName()) ) {
                foundReactiveObjectInterface = true;
            }
        }
        // the ReactiveObject interface method are injected by the bytecode instrumenter, better check they are indeed available..
        boolean containsGetLeftTuple = checkContainsMethod(personClass, "getLeftTuples");
        boolean containsAddLeftTuple = checkContainsMethod(personClass, "addLeftTuple");
        boolean containsRemoveLeftTuple = checkContainsMethod(personClass, "removeLeftTuple");
        
        boolean foundReactiveInjectedMethods = false;
        for ( Method m : personClass.getMethods() ){
            if ( m.getName().startsWith(BytecodeInjectReactive.DROOLS_PREFIX) ) {
                foundReactiveInjectedMethods = true;
            }
        }
        return foundReactiveObjectInterface
                && containsGetLeftTuple && containsAddLeftTuple && containsRemoveLeftTuple
                && foundReactiveInjectedMethods ;
    }

    private boolean checkContainsMethod(Class<?> personClass, Object methodName) {
        for ( Method m : personClass.getMethods() ){
            if (m.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

}
