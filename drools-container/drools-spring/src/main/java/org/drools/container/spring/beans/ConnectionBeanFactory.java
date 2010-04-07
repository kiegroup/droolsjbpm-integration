package org.drools.container.spring.beans;

import org.drools.grid.generic.GenericConnection;
import org.drools.grid.local.LocalConnection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ConnectionBeanFactory implements FactoryBean, InitializingBean {

	private String id;
	private String type;
	private Object connection;

	public Object getObject() throws Exception {
		return connection;
	}

	public Class<GenericConnection> getObjectType() {
		return GenericConnection.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		if ("local".equals(type)) {
			connection = new LocalConnection();
		}
		else if ("remote".equals(type)) {
			throw new UnsupportedOperationException("not implemented yet");
		}
		else {
			throw new IllegalArgumentException("invalid connection type: local/remote");
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
