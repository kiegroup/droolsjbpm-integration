package org.drools.container.spring.beans;

import org.drools.grid.generic.GenericConnection;
import org.drools.grid.local.LocalConnection;
import org.drools.io.ResourceChangeScanner;
import org.drools.io.ResourceFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ResourceChangeScannerBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private String id;
    private int interval;

    public Object getObject() throws Exception {
        return ResourceFactory.getResourceChangeScannerService();
    }

    public Class<ResourceChangeScanner> getObjectType() {
        return ResourceChangeScanner.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
    	ResourceFactory.getResourceChangeScannerService().setInterval( this.interval );
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}



}
