/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.container.spring.beans;

import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.impl.UrlResource;
import org.drools.io.internal.InternalResource;
import org.springframework.beans.factory.InitializingBean;

public class DroolsResourceAdapter
    implements
    InitializingBean {
    private Resource              resource;
    private ResourceType          resourceType;
    private ResourceConfiguration resourceConfiguration;

    public DroolsResourceAdapter() {

    }

    public DroolsResourceAdapter(String resource,
                                 ResourceType resourceType,
                                 ResourceConfiguration resourceConfiguration) {
        super();
        setResource( resource );
        this.resourceType = resourceType;
        this.resourceConfiguration = resourceConfiguration;
    }

    public void setResource(String resource) {
        if ( resource.trim().startsWith( "classpath:" ) ) {
            this.resource = new ClassPathResource( resource.substring( resource.indexOf( ':' ) + 1 ),
                                                   ClassPathResource.class.getClassLoader() );
        } else {
            this.resource = new UrlResource( resource );
        }
    }

    public void setBasicAuthenticationEnabled(Boolean enabled) {
        if ( enabled && !(this.resource instanceof UrlResource) ) {
            throw new IllegalArgumentException( "Authentication Attributes are only valid for URL Resources" );
        }

        if ( this.resource instanceof UrlResource ) {
            ((UrlResource) this.resource).setBasicAuthentication( enabled ? "enabled" : "disabled" );
        }
    }

    public void setBasicAuthenticationUsername(String username) {
        if ( !(this.resource instanceof UrlResource) ) {
            throw new IllegalArgumentException( "Authentication Attributes are only valid for URL Resources" );
        }
        ((UrlResource) this.resource).setUsername( username );
    }

    public void setBasicAuthenticationPassword(String password) {
        if ( !(this.resource instanceof UrlResource) ) {
            throw new IllegalArgumentException( "Authentication Attributes are only valid for URL Resources" );
        }
        ((UrlResource) this.resource).setPassword( password );
    }

    public void setName(String name){
        if ( !(this.resource instanceof InternalResource) ) {
            throw new IllegalArgumentException( "'name' attribute is only valid for InternalResource subclasses" );
        }
        ((InternalResource) this.resource).setName( name );
    }
    
    public void setDescription(String description){
        if ( !(this.resource instanceof InternalResource) ) {
            throw new IllegalArgumentException( "'description' attribute is only valid for InternalResource subclasses" );
        }
        ((InternalResource) this.resource).setDescription( description );
    }
    
    public DroolsResourceAdapter(String resource,
                                 ResourceType resourceType) {
        this( resource,
              resourceType,
              null );
    }

    public DroolsResourceAdapter(String resource) {
        this( resource,
              ResourceType.DRL,
              null );
    }

    public DroolsResourceAdapter(String resource,
                                 String resourceType,
                                 ResourceConfiguration resourceConfiguration) {
        this( resource,
              ResourceType.getResourceType( resourceType ),
              resourceConfiguration );
    }

    public DroolsResourceAdapter(String resource,
                                 String resourceType) {
        this( resource,
              resourceType,
              null );
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
        if ( resource == null ) {
            throw new IllegalArgumentException( "resource property is mandatory" );
        }
        if ( resourceType == null ) {
            throw new IllegalArgumentException( "resourceType property is mandatory" );
        }
        if ( resourceConfiguration != null && !(ResourceType.DTABLE.equals( resourceType ) || ResourceType.XSD.equals( resourceType )) ) {
            throw new IllegalArgumentException( "Only Decision Tables or XSD resources can have configuration" );
        }
    }
}
