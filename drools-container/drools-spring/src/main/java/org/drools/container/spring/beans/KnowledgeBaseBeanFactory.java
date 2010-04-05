package org.drools.container.spring.beans;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.vsm.ServiceManager;
import org.drools.vsm.local.ServiceManagerLocalClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class KnowledgeBaseBeanFactory implements FactoryBean, InitializingBean {

	private KnowledgeBase kbase;
	private ServiceManager serviceManager;
	//XXX currently the SessionManager only has one kbase.
//	private String name;
//	private String beanName;
	private List<DroolsResourceAdapter> resources = Collections.emptyList();
	private List<DroolsResourceAdapter> models = Collections.emptyList();

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
			System.out.println("creating NEW SERVICE MANAGER LOCAL CLIENT");
			serviceManager = new ServiceManagerLocalClient();
		}

		KnowledgeBuilder kbuilder = getServiceManager().getKnowledgeBuilderFactoryService().newKnowledgeBuilder();
		kbase = getServiceManager().getKnowledgeBaseFactoryService().newKnowledgeBase();

		if (models != null && models.size() > 0) {
			for (DroolsResourceAdapter res: models) {
				Options xjcOptions = new Options();
				xjcOptions.setSchemaLanguage(Language.XMLSCHEMA);
				try {
					KnowledgeBuilderHelper.addXsdModel(res.getDroolsResource(),
														kbuilder,
														xjcOptions, 
														"xsd" );
					kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
				} catch (IOException e) {
					throw new RuntimeException("Error creating XSD model", e);
				}
			}
		}
		
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
		
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

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

	public void setModels(List<DroolsResourceAdapter> models) {
		this.models = models;
	}

	public List<DroolsResourceAdapter> getModels() {
		return models;
	}
}
