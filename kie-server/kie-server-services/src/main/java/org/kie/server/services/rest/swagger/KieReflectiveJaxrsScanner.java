package org.kie.server.services.rest.swagger;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.collection.JavaConverters;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.jaxrs.config.JaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;

public class KieReflectiveJaxrsScanner implements JaxrsScanner {

    private Logger logger = LoggerFactory.getLogger(KieReflectiveJaxrsScanner.class);
    
    private Set<String> resourceInterfacePackages = new HashSet<String>();

    public Set<String> getResourcePackages() { 
        return resourceInterfacePackages;
    }

    /**
     * Add package names that should be scanned for classes that have the Swagger {@link Api} annotation. 
     * @param restApiPackage A package name
     */
    public void addResourcePackages(String... restApiPackage) { 
        if( restApiPackage.length == 1 ) { 
            this.resourceInterfacePackages.add(restApiPackage[0]);
        } else if( restApiPackage.length > 1 ) { 
            this.resourceInterfacePackages.addAll(Arrays.asList(restApiPackage));
        }
    } 

    /**
     * Configure Reflections and use the Reflections to scan for classes with the Swagger {@link Api} annotation.
     */
    @Override
    public scala.collection.immutable.List<Class<?>> classesFromContext(Application app, ServletConfig sc) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        Set<URL> pkgLocUrls = new HashSet<URL>();
        for( String restApiPkg : resourceInterfacePackages ) { 
            Set<URL> urls = ClasspathHelper.forPackage(restApiPkg);
            if( urls.isEmpty() ) { 
                logger.warn("No location found for  package '{}': this package will be skipped!", restApiPkg);
                continue;
            }
            pkgLocUrls.addAll(urls);
        }
        configBuilder.setUrls(pkgLocUrls);
        configBuilder.setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
        Reflections reflections = new Reflections(configBuilder);
       
        Set<Class<?>> annotatedWithSwaggerApiAnno = reflections.getTypesAnnotatedWith(Api.class);
      
        scala.collection.immutable.List<Class<?>> resultList
            = JavaConverters.asScalaIterableConverter(annotatedWithSwaggerApiAnno).asScala().toList();
        
        if( logger.isDebugEnabled() ) { 
            logger.debug( "Swagger will provide inof on the following classes:");
            for( Class<?> apiClass : JavaConverters.asJavaListConverter(resultList).asJava() ) { 
                logger.debug( apiClass.getName());
            }
        }
        
        return resultList;
    }

    /**
     * This method is never used, as far as I can tell? 
     * It seems to be for other Swagger functionality that's not used with JAXRS/Resteasy.
     */
    @Override
    public scala.collection.immutable.List<Class<?>> classes() {
        new Throwable().printStackTrace();
        
        List<Class<?>> emptyClassList = Arrays.asList(new Class<?>[0]);
        return JavaConverters.asScalaIterableConverter(emptyClassList).asScala().toList();
    }
}
