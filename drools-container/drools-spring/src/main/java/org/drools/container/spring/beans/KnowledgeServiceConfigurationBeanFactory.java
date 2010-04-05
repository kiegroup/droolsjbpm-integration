package org.drools.container.spring.beans;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.drools.KnowledgeBase;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.common.InternalRuleBase;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.server.profile.KnowledgeServiceConfiguration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeServiceConfigurationBeanFactory implements FactoryBean, InitializingBean {

	private KnowledgeServiceConfiguration service;
	private String id;
	private String sessionId;
	private CommandExecutor session;
	private String marshaller;
	private List<String> classes = Collections.emptyList();
	private List<String> commands = Collections.emptyList();

	public Object getObject() throws Exception {
		return service;
	}

	public Class<? extends KnowledgeServiceConfiguration> getObjectType() {
		return KnowledgeServiceConfiguration.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		JAXBContext context = null;
		if (classes != null && classes.size() > 0) {
			KnowledgeBase kbase = null;
			if (session instanceof StatelessKnowledgeSession) {
				InternalRuleBase ruleBase = ((StatelessKnowledgeSessionImpl)session).getRuleBase();
				kbase = new KnowledgeBaseImpl(ruleBase);
			} else if (session instanceof StatefulKnowledgeSessionImpl) {
				kbase = ((StatefulKnowledgeSessionImpl) session).getKnowledgeBase();
			} else {
			    throw new IllegalArgumentException("Unable to set ClassLoader on " + session);
			}
			context = KnowledgeBuilderHelper.newJAXBContext(classes.toArray(new String[classes.size()]), kbase);
		}
		service = new KnowledgeServiceConfiguration(id, sessionId, session, marshaller, context, commands);
	}

	public void setService(KnowledgeServiceConfiguration service) {
		this.service = service;
	}

	public KnowledgeServiceConfiguration getService() {
		return service;
	}

	public void setId(String id) {
		this.id = id; 
	}

	public String getId() {
		return id;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSession(CommandExecutor session) {
		this.session = session;
	}

	public CommandExecutor getSession() {
		return session;
	}

	public void setMarshaller(String marshaller) {
		this.marshaller = marshaller;
	}

	public String getMarshaller() {
		return marshaller;
	}

	public void setClasses(List<String> classes) {
		this.classes = classes;
	}

	public List<String> getClasses() {
		return classes;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public List<String> getCommands() {
		return commands;
	}

}
