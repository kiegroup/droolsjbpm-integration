package org.kie.server.services.jbpm.taskqueries.util;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory responsible for building the database-specific {@link TaskQueriesStrategy}. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class TaskQueriesStrategyFactory {

	private static final Logger logger = LoggerFactory.getLogger(TaskQueriesStrategyFactory.class);
	
	//TODO Should we do this with a KieServerConfig?
	private static final String DB_STRATEGY = System.getProperty(KieServerConstants.CFG_PERSISTANCE_DIALECT);
	
	private KieServerRegistry context;
	
	private TaskQueriesStrategy strategy;
	
	public TaskQueriesStrategyFactory(KieServerRegistry context) {
		this.context = context;
		switch (context.getConfig().getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")) {
		case "org.hibernate.dialect.PostgreSQLDialect":
			logger.info("Initializing PostgreSQL TaskQuery strategy.");
			this.strategy = new PostgreSQLTaskQueriesStrategy();
		case "org.hibernate.dialect.H2Dialect":
			logger.info("Initializing H2 TaskQuery strategy.");
			this.strategy = new H2TaskQueriesStrategy();
		default:
			logger.info("Initializing default H2 TaskQuery strategy.");
			this.strategy = new H2TaskQueriesStrategy();
		}
	}
	
	public TaskQueriesStrategy getTaskQueriesStrategy() {
		return strategy;
	
	}
	
	
}
