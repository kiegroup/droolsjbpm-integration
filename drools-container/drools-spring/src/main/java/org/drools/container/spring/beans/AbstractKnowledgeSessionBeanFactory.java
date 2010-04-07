package org.drools.container.spring.beans;

import org.drools.KnowledgeBase;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.grid.ExecutionNode;
import org.drools.runtime.CommandExecutor;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NamedBean;

public abstract class AbstractKnowledgeSessionBeanFactory implements FactoryBean,
		InitializingBean, BeanNameAware, NamedBean {

	private ExecutionNode node;
	private KnowledgeBase kbase;
	private String beanName;
	private String name;

	public AbstractKnowledgeSessionBeanFactory() {
		super();
	}

	public Object getObject() throws Exception {
		return getCommandExecutor();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public KnowledgeBase getKbase() {
		return kbase;
	}

	public void setKbase(KnowledgeBase kbase) {
		this.kbase = kbase;
	}

	public boolean isSingleton() {
		return true;
	}

	public final void afterPropertiesSet() throws Exception {
		if (kbase == null) {
			throw new IllegalArgumentException("kbase property is mandatory");
		}
		if (name == null) {
			name = beanName;
		}
		internalAfterPropertiesSet();
		if (node != null) {
			node.get(DirectoryLookupFactoryService.class).register(name, getCommandExecutor());
		}
	}

	protected abstract CommandExecutor getCommandExecutor();

	protected abstract void internalAfterPropertiesSet();

	public ExecutionNode getNode() {
		return node;
	}

	public void setNode(ExecutionNode node) {
		this.node = node;
	}

	public void setBeanName(String name) {
		this.beanName = name;

	}

	public String getBeanName() {
		return beanName;
	}

}
