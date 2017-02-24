package org.kie.server.services.jbpm.taskqueries.util;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieServerRegistry;

public class TaskQueriesStrategyFactory {

	//TODO Should we do this with a KieServerConfig?
	private static final String DB_STRATEGY = System.getProperty(KieServerConstants.CFG_PERSISTANCE_DIALECT);
	
	private KieServerRegistry context;
	
	private TaskQueriesStrategy strategy;
	
	
	public TaskQueriesStrategyFactory(KieServerRegistry context) {
		this.context = context;
		switch (context.getConfig().getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")) {
		case "org.hibernate.dialect.PostgreSQLDialect":
			this.strategy = new PostgreSQLTaskQueriesStrategy();
		case "org.hibernate.dialect.H2Dialect":
			this.strategy = new H2TaskQueriesStrategy();
		default:
			this.strategy = new H2TaskQueriesStrategy();
		}
	}
	
	public TaskQueriesStrategy getTaskQueriesStrategy() {
		return strategy;
	
	}
	
	
}
