package org.kie.remote.common.rest.swagger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import scala.Tuple3;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;
import scala.collection.mutable.ListBuffer;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.MutableParameter;
import com.wordnik.swagger.jaxrs.reader.BasicJaxrsReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;

/**
 * This class can be used to replace the {@link BasicJaxrsReader} instance
 * if for some reason we need to debug it -- or maybe hack more missing 
 * functionality into Swagger.
 */
public class KieDebugJaxrsApiReader extends BasicJaxrsReader {

    private Logger logger = LoggerFactory.getLogger(KieDebugJaxrsApiReader.class);
    
    @Override
    public Class<?> findSubresourceType( Method method ) {
        log();
        return super.findSubresourceType(method);
    }

    @Override
    public List<Field> getAllFields( Class<?> cls ) {
        log();
        return super.getAllFields(cls);
    }

    @Override
    public List<Parameter> getAllParamsFromFields( Class<?> cls ) {
        log();
        return super.getAllParamsFromFields(cls);
    }

    @Override
    public void parseApiParamAnnotation( MutableParameter param, ApiParam annotation ) {
        log();
        super.parseApiParamAnnotation(param, annotation);
        
    }

    @Override
    public String parseHttpMethod( Method method, ApiOperation op ) {
        log();
        return super.parseHttpMethod(method, op);
    }

    @Override
    public Operation parseOperation( 
            Method method, ApiOperation apiOperation, List<ResponseMessage> apiResponses, 
            String isDeprecated, 
            List<Parameter> parentParams, ListBuffer<Method> parentMethods ) {
        log();
        return super.parseOperation(method, apiOperation, apiResponses, isDeprecated, parentParams, parentMethods);
    }

    @Override
    public String pathFromMethod( Method method ) {
        log();
        return super.pathFromMethod(method);
    }

    @Override
    public String processDataType( Class<?> paramType, Type genericParamType ) {
        log();
        return super.processDataType(paramType, genericParamType);
    }

    @Override
    public List<Parameter> processParamAnnotations( MutableParameter mutable, Annotation[] paramAnnotations ) {
        log();
        return super.processParamAnnotations(mutable, paramAnnotations);
    }

    @Override
    public Option<ApiListing> read( String docRoot, Class<?> cls, SwaggerConfig config ) {
        log();
        Option<ApiListing> apiListOption = super.read(docRoot, cls, config);
        if( logger.isInfoEnabled() ) { 
            if( apiListOption.isDefined() ) { 
                ApiListing apiList = apiListOption.get();
                logger.info( "ApiListing: " + apiList.basePath() + "|" + apiList.apiVersion() + "|" + apiList.apis().size() );
                for( ApiDescription apiDesc : JavaConverters.asJavaCollectionConverter(apiList.apis()).asJavaCollection() ) { 
                    logger.info( "ApiDescription: " + apiDesc.path());
                }
            }
        }
        return apiListOption;
    }

    @Override
    public Option<Operation> readMethod( Method method, List<Parameter> parentParams, ListBuffer<Method> parentMethods ) {
        log();
        return super.readMethod(method, parentParams, parentMethods);
    }

    @Override
    public Option<ApiListing> readRecursive( 
            String docRoot, String parentPath, 
            Class<?> cls, SwaggerConfig config,
            ListBuffer<Tuple3<String, String, ListBuffer<Operation>>> operations, 
            ListBuffer<Method> parentMethods ) {
        log();
        return super.readRecursive(docRoot, parentPath, cls, config, operations, parentMethods);
    }

    @Override
    public String readString( String value, String defaultValue, String ignoreValue ) {
        log();
        return super.readString(value, defaultValue, ignoreValue);
    } 

    private void log() { 
        logger.info( "> " + Thread.currentThread().getStackTrace()[2].getMethodName() );
    }
}
