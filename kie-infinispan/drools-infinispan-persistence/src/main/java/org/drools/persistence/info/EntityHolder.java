/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.drools.persistence.info;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.persistence.api.PersistentSession;
import org.drools.persistence.api.PersistentWorkItem;
import org.drools.persistence.util.Base64;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

@Entity
@Indexed
public class EntityHolder {

	@Id @DocumentId @Field
	private String key;
	@Field
	private String type;
	@Field
	private Long sessionInfoId;
	@Field
	private Integer sessionInfoVersion;
	@Field
	private String sessionInfoData;
	@Field
	private Date sessionInfoLastModificationDate;
	@Field
	private Date sessionInfoStartDate;
	@Field
	private Long workItemInfoId;
	@Field
	private String workItemInfoName;
	@Field
	private Integer workItemInfoVersion;
	@Field
	private Long workItemInfoProcessInstanceId;
	@Field
	private Long workItemInfoState;
	@Field
	private Date workItemInfoCreationDate;
	@Field
	private String workItemInfoByteArray;

	public EntityHolder(String key, PersistentSession session) {
		this.key = key;
		this.type = "sessionInfo";
		this.sessionInfoId = session.getId();
		SessionInfo sessionInfo = (SessionInfo) session;
		this.sessionInfoVersion = sessionInfo.getVersion();
		session.transform();
		this.sessionInfoData = Base64.encodeBase64String(sessionInfo.getData());
		this.sessionInfoLastModificationDate = sessionInfo.getLastModificationDate();
		this.sessionInfoStartDate = sessionInfo.getStartDate();
	}

	public EntityHolder(String key, PersistentWorkItem workItem) {
		this.key = key;
		this.type = "workItemInfo";
		workItem.transform();
		this.workItemInfoId = workItem.getId();
		WorkItemInfo workItemInfo = (WorkItemInfo) workItem;
		this.workItemInfoName = workItemInfo.getName();
		this.workItemInfoVersion = workItemInfo.getVersion();
		this.workItemInfoProcessInstanceId = workItemInfo.getProcessInstanceId();
		this.workItemInfoState = workItemInfo.getState();
		this.workItemInfoCreationDate = workItemInfo.getCreationDate();
		this.workItemInfoByteArray = Base64.encodeBase64String(workItemInfo.getWorkItemByteArray());
	}

	protected EntityHolder(String key, String type) {
		this.key = key;
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public SessionInfo getSessionInfo() {
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setId(this.sessionInfoId);
		sessionInfo.setData(Base64.decodeBase64(this.sessionInfoData));
		sessionInfo.setLastModificationDate(this.sessionInfoLastModificationDate);
		try {
			java.lang.reflect.Field versionField = SessionInfo.class.getField("version");
			versionField.setAccessible(true);
			versionField.set(sessionInfo, this.sessionInfoVersion);
			java.lang.reflect.Field startDateField = SessionInfo.class.getField("startDate");
			startDateField.setAccessible(true);
			startDateField.set(sessionInfo, this.sessionInfoStartDate);
		} catch (Exception e) { /* TODO */ }

		return sessionInfo;
	}

	public void setSessionInfo(SessionInfo sessionInfo) {
		this.sessionInfoId = sessionInfo.getId();
		this.sessionInfoVersion = sessionInfo.getVersion();
		sessionInfo.transform();
		this.sessionInfoData = Base64.encodeBase64String(sessionInfo.getData());
		this.sessionInfoLastModificationDate = sessionInfo.getLastModificationDate();
		this.sessionInfoStartDate = sessionInfo.getStartDate();
	}

	public WorkItemInfo getWorkItemInfo() {
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setName(this.workItemInfoName);
		workItem.setProcessInstanceId(this.workItemInfoProcessInstanceId);
		WorkItemInfo workItemInfo = new WorkItemInfo(workItem, null);
		workItemInfo.setId(this.workItemInfoId);
		try {
			java.lang.reflect.Field versionField = WorkItemInfo.class.getDeclaredField("version");
			versionField.setAccessible(true);
			versionField.set(workItemInfo, this.workItemInfoVersion);
			java.lang.reflect.Field stateField = WorkItemInfo.class.getDeclaredField("state");
			stateField.setAccessible(true);
			stateField.set(workItemInfo, this.workItemInfoState);
			java.lang.reflect.Field creationDateField = WorkItemInfo.class.getDeclaredField("creationDate");
			creationDateField.setAccessible(true);
			creationDateField.set(workItemInfo, this.workItemInfoCreationDate);
			java.lang.reflect.Field workItemByteArrayField = WorkItemInfo.class.getDeclaredField("workItemByteArray");
			workItemByteArrayField.setAccessible(true);
			workItemByteArrayField.set(workItemInfo, Base64.decodeBase64(this.workItemInfoByteArray));
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return workItemInfo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setWorkItemInfo(WorkItemInfo workItemInfo) {
		workItemInfo.transform();
		this.workItemInfoId = workItemInfo.getId();
		this.workItemInfoName = workItemInfo.getName();
		this.workItemInfoVersion = workItemInfo.getVersion();
		this.workItemInfoProcessInstanceId = workItemInfo.getProcessInstanceId();
		this.workItemInfoState = workItemInfo.getState();
		this.workItemInfoCreationDate = workItemInfo.getCreationDate();
		this.workItemInfoByteArray = Base64.encodeBase64String(workItemInfo.getWorkItemByteArray());
	}
}
