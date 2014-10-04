package org.kie.server.services.rest.swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.jaxrs.config.WebXMLReader;
import com.wordnik.swagger.jaxrs.reader.BasicJaxrsReader;
import com.wordnik.swagger.reader.ClassReaders;

public class KieSwaggerJaxrsConfig extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(KieSwaggerJaxrsConfig.class);
    
    /** generated serial version uid */
    private static final long serialVersionUID = 3063592528647027604L;

    /**
     * No error checking or cleaning up of the 'swagger.api.packages' property!
     * 
     * Please make sure that this property
     * - does NOT end or start with a "," 
     * - does NOT end or start with extra spaces
     */
    public static final String SWAGGER_API_PACKAGES_WEB_XML_PARAM = "swagger.api.packages";
    
    @Override
    public void init( ServletConfig servletConfig ) throws ServletException {
        super.init(servletConfig);

        logger.debug("Initializing Swagger configuration");
        
        // 1. get the config values (api version, base path) we want from the web.xml
        ConfigFactory.setConfig(new WebXMLReader(servletConfig));

        // 2. force swagger to (reflectively) scan the packages we want it to
        KieReflectiveJaxrsScanner scanner = new KieReflectiveJaxrsScanner();
        
        // No error checking or cleaning up of the 'swagger.api.packages' property!
        String swaggerApiClassParam = servletConfig.getInitParameter(SWAGGER_API_PACKAGES_WEB_XML_PARAM);
        String[] swaggerApiPkgs = swaggerApiClassParam.split(",");
        if( logger.isDebugEnabled() ) { 
            for( String apiPkg : swaggerApiPkgs ) { 
                logger.debug("Classes in package '{}' will be scanned for JAX-RS and Swagger annotations.", apiPkg ); 
            }
        }
        scanner.addResourcePackages(swaggerApiPkgs);
        ScannerFactory.setScanner(scanner);
        
        // 3. set the API reader (part of initialization of Swagger.. )
        // (using the BasicJaxrsReader ensures that methods that do NOT have the @ApiOperation annotation 
        //  are also processed by Swagger)
        ClassReaders.setReader(new BasicJaxrsReader());
    }

}
