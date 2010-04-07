package org.drools.container.spring.beans;

import org.drools.grid.ExecutionNode;
import org.drools.grid.generic.GenericConnection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ExecutionNodeBeanFactory  implements FactoryBean, InitializingBean {

	private String id;
	private GenericConnection connection;
	private ExecutionNode node;

	public Object getObject() throws Exception {
		return node;
	}

	public Class<? extends ExecutionNode> getObjectType() {
		return ExecutionNode.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		node = connection.getExecutionNode(null);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setConnection(GenericConnection connection) {
		this.connection = connection;
	}

	public GenericConnection getConnection() {
		return connection;
	}

}
