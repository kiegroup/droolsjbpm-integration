package org.kie.server.jbpm.taskqueries.api.model.definition;

/**
 * HumanTask fields.
 * <p/>
 * These are the filterable fields in our TaskQuery API. 
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public enum TaskField {

	//@formatter:off
	ID,			
	ACTIVATIONTIME,		
	ACTUALOWNER,			
	CREATEDBY,			
	CREATEDON,			
	DEPLOYMENTID,			
	DESCRIPTION,			
	DUEDATE,			
	NAME,			
	PARENTID,			
	PRIORITY,			
	PROCESSID,			
	PROCESSINSTANCEID,			
	PROCESSSESSIONID,			
	STATUS,			
	TASKID,			
	WORKITEMID
	//@formatter:on
}
