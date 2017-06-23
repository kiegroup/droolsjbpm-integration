package org.drools.compiler.xpath.tobeinstrumented;

import static org.kie.maven.plugin.InjectReactiveMojo.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.drools.core.phreak.ReactiveObject;
import org.junit.Test;

public class InjectReactiveMojoConfigTest {

    @Test
    public void testRegexpForPackagesDefault() {
        String[] inputConfig = new String[]{"*"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertTrue(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertTrue(isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertTrue(isPackageNameIncluded("xyz.my", config));
    }
    
    @Test
    public void testRegexpForPackagesSingleNoStars() {
        String[] inputConfig = new String[]{"org.drools"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertFalse(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded("xyz.my", config));
    }
    
    @Test
    public void testRegexpForPackagesMultipleNoStars() {
        String[] inputConfig = new String[]{"org.drools", "xyz.my"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertFalse(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertTrue (isPackageNameIncluded("xyz.my", config));
    }
    
    @Test
    public void testRegexpForPackagesSingleStars() {
        String[] inputConfig = new String[]{"org.drools.*"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertFalse(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertTrue (isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded("xyz.my", config));
    }
    
    @Test
    public void testRegexpForPackagesMultipleStars() {
        String[] inputConfig = new String[]{"org.drools.*", "xyz.my.*"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertFalse(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertTrue (isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertTrue (isPackageNameIncluded("xyz.my", config));
    }
    
    @Test
    public void testRegexpForPackagesCheckPart() {
        String[] inputConfig = new String[]{"my"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertFalse(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded("xyz.my", config));
    }
    
    @Test
    public void testRegexpForPackagesCheckNaming() {
        String[] inputConfig = new String[]{"org.drools", "to.instrument.*"};
        
        List<String> config = convertAllToPkgRegExps(inputConfig);
        
        System.out.println(Arrays.asList(config));
        
        assertFalse(isPackageNameIncluded(Object.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded(ReactiveObject.class.getPackage().getName(), config));
        assertFalse(isPackageNameIncluded("xyz.my", config));
        assertTrue (isPackageNameIncluded("to.instrument", config));
        assertFalse(isPackageNameIncluded("to.not.instrument", config));
    }
}
