package org.drools.container.spring.beans;

import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.impl.UrlResource;
import org.springframework.beans.factory.InitializingBean;

public class DroolsResourceAdapter implements InitializingBean {
	private Resource resource;
	private ResourceType resourceType;
	private ResourceConfiguration resourceConfiguration;
	
	public DroolsResourceAdapter() {
		
	}
	
	public DroolsResourceAdapter(String resource, ResourceType resourceType,
			ResourceConfiguration resourceConfiguration) {
		super();
		setResource(resource);
		this.resourceType = resourceType;
		this.resourceConfiguration = resourceConfiguration;
	}

	public void setResource(String resource) {
		if ( resource.trim().startsWith( "classpath:" ) ) {
			this.resource = new ClassPathResource( 
					resource.substring( resource.indexOf( ':' ) + 1 ), 
						ClassPathResource.class.getClassLoader() );
        } else {
        	this.resource = new UrlResource( resource );
        }
	}
	
	public DroolsResourceAdapter(String resource, ResourceType resourceType) {
		this(resource, resourceType, null);
	}

	public DroolsResourceAdapter(String resource) {
		this(resource, ResourceType.DRL, null);
	}

	public DroolsResourceAdapter(String resource, String resourceType,
			ResourceConfiguration resourceConfiguration) {
		this(resource, ResourceType.getResourceType(resourceType), resourceConfiguration);
	}

	public DroolsResourceAdapter(String resource, String resourceType) {
		this(resource, resourceType, null);
	}
	
	public Resource getDroolsResource() {
		return resource;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public ResourceConfiguration getResourceConfiguration() {
		return resourceConfiguration;
	}

	public void setResourceConfiguration(ResourceConfiguration resourceConfiguration) {
		this.resourceConfiguration = resourceConfiguration;
	}

	public void afterPropertiesSet() throws Exception {
		if (resource == null) {
			throw new IllegalArgumentException("resource property is mandatory");
		}
		if (resourceType == null) {
			throw new IllegalArgumentException("resourceType property is mandatory");
		}
		if (resourceConfiguration != null && !ResourceType.DTABLE.equals(resourceType)) {
			throw new IllegalArgumentException("Only Decision Tables can have configuration");
		}
	}
}
