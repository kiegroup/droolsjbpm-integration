package org.drools.container.spring.beans;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.PackageScanClassResolver;
import org.drools.server.KnowledgeService;
import org.drools.server.KnowledgeServiceImpl;
import org.drools.server.profile.KnowledgeServiceConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeServiceBeanFactory  implements FactoryBean, InitializingBean {

	private String id;
	private CamelContext camelContext;
	private PackageScanClassResolver packageClassLoaderResolver;
	private KnowledgeService service;
	private List<KnowledgeServiceConfiguration> configurations = Collections.emptyList();
	private String nodeId;

	public Object getObject() throws Exception {
		return service;
	}

	public Class<? extends KnowledgeService> getObjectType() {
		return KnowledgeService.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		Map<String, KnowledgeServiceConfiguration> configs = new HashMap<String, KnowledgeServiceConfiguration>();
		for (KnowledgeServiceConfiguration cfg : configurations) {
			configs.put(cfg.getSessionId(), cfg);
		}
		service = new KnowledgeServiceImpl(camelContext, configs, nodeId);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setService(KnowledgeService service) {
		this.service = service;
	}

	public KnowledgeService getService() {
		return service;
	}

	public void setConfigurations(List<KnowledgeServiceConfiguration> configurations) {
		this.configurations = configurations;
	}

	public List<KnowledgeServiceConfiguration> getConfigurations() {
		return configurations;
	}

	public void setPackageClassLoaderResolver(PackageScanClassResolver packageClassLoaderResolver) {
		this.packageClassLoaderResolver = packageClassLoaderResolver;
	}

	public PackageScanClassResolver getPackageClassLoaderResolver() {
		return packageClassLoaderResolver;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return nodeId;
	}

}
