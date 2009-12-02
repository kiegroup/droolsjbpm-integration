package org.drools.container.spring.beans;

import java.util.Collections;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.vsm.ServiceManager;
import org.drools.vsm.local.ServiceManagerLocalClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class KnowledgeBaseBeanFactory implements FactoryBean, InitializingBean {

	private KnowledgeBase kbase;
	private ServiceManager serviceManager;
	//XXX currently the SessionManager only has one kbase.
//	private String name;
//	private String beanName;
	private List<DroolsResourceAdapter> resources = Collections.emptyList();

	public Object getObject() throws Exception {
		return kbase;
	}

	public Class<? extends KnowledgeBase> getObjectType() {
		return KnowledgeBase.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
//		if (name == null) {
//			setName(getBeanName());
//		}
		if (serviceManager == null) {
			serviceManager = new ServiceManagerLocalClient();
		}
		KnowledgeBuilder kbuilder = getServiceManager().getKnowledgeBuilderFactory().newKnowledgeBuilder();
		for (DroolsResourceAdapter res: resources) {
			if (res.getResourceConfiguration() == null) {
				kbuilder.add(res.getDroolsResource(), res.getResourceType());
			} else {
				kbuilder.add(res.getDroolsResource(), res.getResourceType(), res.getResourceConfiguration());
			}
		}
		
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (!errors.isEmpty() ) {
			throw new RuntimeException(errors.toString());
		}
		
//		kbase = getServiceManager().getKnowledgeBaseFactory().newKnowledgeBase(getName());
		kbase = getServiceManager().getKnowledgeBaseFactory().newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		///
		
	}

	public KnowledgeBase getKbase() {
		return kbase;
	}

	public void setKbase(KnowledgeBase kbase) {
		this.kbase = kbase;
	}

//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
	
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

//	public String getBeanName() {
//		return beanName;
//	}
//
//	public void setBeanName(String name) {
//		beanName = name;
//	}

	public List<DroolsResourceAdapter> getResources() {
		return resources;
	}

	public void setResources(List<DroolsResourceAdapter> resources) {
		this.resources = resources;
	}

	public void setServiceManager(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}
}
