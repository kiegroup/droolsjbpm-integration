package org.drools.container.spring.beans;

import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.impl.UrlResource;
import org.springframework.beans.factory.FactoryBean;

public class ResourceBeanFactory implements FactoryBean {

	private Resource resource;
	
	public ResourceBeanFactory(String source) {
		if ( source.trim().startsWith( "classpath:" ) ) {
            resource = new ClassPathResource( source.substring( source.indexOf( ':' ) + 1 ), ClassPathResource.class.getClassLoader() );
        } else {
            resource = new UrlResource( source );
        }
	}
	
	public Object getObject() throws Exception {
		return resource;
	}

	public Class<Resource> getObjectType() {
		return Resource.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
