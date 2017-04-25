/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.jbpm.queries.util;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory responsible for building the database-specific {@link QueryStrategy}.
 */
public class QueryStrategyFactory {

	private static final Logger logger = LoggerFactory.getLogger(QueryStrategyFactory.class);
	
	private QueryStrategy processStrategy;
	
	private QueryStrategy taskStrategy;
	
	public QueryStrategyFactory(KieServerRegistry context) {
		switch (context.getConfig().getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect")) {
		case "org.hibernate.dialect.PostgreSQLDialect":
			logger.info("Initializing PostgreSQL Query strategies.");
			this.processStrategy = new PostgreSQLProcessInstanceQueryStrategy();
			this.taskStrategy = new PostgreSQLTaskQueryStrategy();
			break;
		case "org.hibernate.dialect.H2Dialect":
			logger.info("Initializing H2 Query strategies.");
			this.processStrategy = new H2ProcessInstanceQueryStrategy();
			this.taskStrategy = new H2TaskQueryStrategy();
			break;
		case "org.hibernate.dialect.Oracle8iDialect":
		case "org.hibernate.dialect.Oracle9iDialect":
		case "org.hibernate.dialect.Oracle10gDialect":
			logger.info("Initializing Oracle Query strategies.");
			this.processStrategy = new OracleProcessInstanceQueryStrategy();
			this.taskStrategy = new OracleTaskQueryStrategy();
			break;
		default:
			logger.info("Initializing default H2 TaskQuery strategy.");
			this.processStrategy = new H2ProcessInstanceQueryStrategy();
			this.taskStrategy = new H2TaskQueryStrategy();
		}
	}
	
	public QueryStrategy getTaskQueriesStrategy() {
		return taskStrategy;
	}
	
	public QueryStrategy getProcessQueriesStrategy() {
		return processStrategy;
	}
	
}
