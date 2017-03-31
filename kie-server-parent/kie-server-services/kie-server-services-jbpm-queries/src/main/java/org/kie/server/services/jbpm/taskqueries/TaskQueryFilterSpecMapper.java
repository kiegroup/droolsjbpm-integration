package org.kie.server.services.jbpm.taskqueries;


/**
 * Maps the TaskQueryFilterSpecs onto QueryFilters for the given database.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class TaskQueryFilterSpecMapper {
	
	
	public enum TASK_FIELDS {

		/*
		 * TODO: Column-type is not important on client-side and can move to the server-side. On the server-side we need to implement a
		 * Strategy pattern that plugs in the correct database strategy based on the Hibernate Dialect configured on KIE-Server. That way,
		 * we can figure out the correct column-types for each dialect we support.
		 */

		//@formatter:off
		
		ID("id", "bigint"),			
		ACTIVATIONTIME("activationtime", "timestamp without timezone"),		
		ACTUALOWNER("actualowner", "String"),			
		CREATEDBY("createdby", "String"),			
		CREATEDON("createdon", "timestamp without timezone"),			
		DEPLOYMENTID("deploymentid", "String"),			
		DESCRIPTION("description", "String"),			
		DUEDATE("duedate", "timestamp without timezone"),			
		NAME("name", "String"),			
		PARENTID("parentid", "bigint"),			
		PRIORITY("priority", "integer"),			
		PROCESSID("processid", "String"),			
		PROCESSINSTANCEID("processinstanceid", "bigint"),			
		PROCESSSESSIONID("processsessionid", "bigint"),			
		STATUS("status", "String"),			
		TASKID("taskid", "bigint"),			
		WORKITEMID("workitemid", "bigint");
		
		//@formatter:on

		private final String columnName;

		private final String columnType;

		private TASK_FIELDS(String columnName, String columnType) {
			this.columnName = columnName;
			this.columnType = columnType;
		}

		public String getColumnName() {
			return columnName;
		}

		public String getColumnType() {
			return columnType;
		}

	};

}
