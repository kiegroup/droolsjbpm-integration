/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.client.serialization.jaxb.impl.deploy;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.PersistenceMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;

@XmlRootElement(name="deployment-descriptor")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDeploymentDescriptor {

	@XmlElement(name="persistence-unit")
	@XmlSchemaType(name="string")
	private String persistenceUnit;

	@XmlElement(name="audit-persistence-unit")
	@XmlSchemaType(name="string")
	private String auditPersistenceUnit;

	@XmlElement(name="audit-mode")
	private AuditMode auditMode = AuditMode.JPA;

	@XmlElement(name="persistence-mode")
	private PersistenceMode persistenceMode = PersistenceMode.JPA;

	@XmlElement(name="runtime-strategy")
	private RuntimeStrategy runtimeStrategy = RuntimeStrategy.SINGLETON;

	@XmlElement(name="marshalling-strategy")
	@XmlElementWrapper(name="marshalling-strategies")
	private List<ObjectModel> marshallingStrategies;

	@XmlElement(name="event-listener")
	@XmlElementWrapper(name="event-listeners")
	private List<ObjectModel> eventListeners;

	@XmlElement(name="task-event-listener")
	@XmlElementWrapper(name="task-event-listeners")
	private List<ObjectModel> taskEventListeners;

	@XmlElement(name="global")
	@XmlElementWrapper(name="globals")
	private List<NamedObjectModel> globals;

	@XmlElement(name="work-item-handler")
	@XmlElementWrapper(name="work-item-handlers")
	private List<NamedObjectModel> workItemHandlers;

	@XmlElement(name="environment-entry")
	@XmlElementWrapper(name="environment-entries")
	private List<NamedObjectModel> environmentEntries;

	@XmlElement(name="configuration")
	@XmlElementWrapper(name="configurations")
	private List<NamedObjectModel> configuration;

	@XmlElement(name="required-role")
	@XmlElementWrapper(name="required-roles")
	private List<String> requiredRoles;

	@XmlElement(name="remoteable-class")
	@XmlElementWrapper(name="remoteable-classes")
	private List<String> classes;

	@XmlElement(name="limit-serialization-classes")
	private Boolean limitSerializationClasses = false;

	public JaxbDeploymentDescriptor() {
		// fox jaxb only
	}

	public JaxbDeploymentDescriptor(String defaultPU) {
		this.persistenceUnit = defaultPU;
		this.auditPersistenceUnit = defaultPU;
	}

    public String getPersistenceUnit() {
        return persistenceUnit;
    }

    public void setPersistenceUnit(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    public String getAuditPersistenceUnit() {
        return auditPersistenceUnit;
    }

    public void setAuditPersistenceUnit(String auditPersistenceUnit) {
        this.auditPersistenceUnit = auditPersistenceUnit;
    }

    public AuditMode getAuditMode() {
        return auditMode;
    }

    public void setAuditMode(AuditMode auditMode) {
        this.auditMode = auditMode;
    }

    public PersistenceMode getPersistenceMode() {
        return persistenceMode;
    }

    public void setPersistenceMode(PersistenceMode persistenceMode) {
        this.persistenceMode = persistenceMode;
    }

    public RuntimeStrategy getRuntimeStrategy() {
        return runtimeStrategy;
    }

    public void setRuntimeStrategy(RuntimeStrategy runtimeStrategy) {
        this.runtimeStrategy = runtimeStrategy;
    }

    public List<ObjectModel> getMarshallingStrategies() {
        return marshallingStrategies;
    }

    public void setMarshallingStrategies(List<ObjectModel> marshallingStrategies) {
        this.marshallingStrategies = marshallingStrategies;
    }

    public List<ObjectModel> getEventListeners() {
        return eventListeners;
    }

    public void setEventListeners(List<ObjectModel> eventListeners) {
        this.eventListeners = eventListeners;
    }

    public List<ObjectModel> getTaskEventListeners() {
        return taskEventListeners;
    }

    public void setTaskEventListeners(List<ObjectModel> taskEventListeners) {
        this.taskEventListeners = taskEventListeners;
    }

    public List<NamedObjectModel> getGlobals() {
        return globals;
    }

    public void setGlobals(List<NamedObjectModel> globals) {
        this.globals = globals;
    }

    public List<NamedObjectModel> getWorkItemHandlers() {
        return workItemHandlers;
    }

    public void setWorkItemHandlers(List<NamedObjectModel> workItemHandlers) {
        this.workItemHandlers = workItemHandlers;
    }

    public List<NamedObjectModel> getEnvironmentEntries() {
        return environmentEntries;
    }

    public void setEnvironmentEntries(List<NamedObjectModel> environmentEntries) {
        this.environmentEntries = environmentEntries;
    }

    public List<NamedObjectModel> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<NamedObjectModel> configuration) {
        this.configuration = configuration;
    }

    public List<String> getRequiredRoles() {
        return requiredRoles;
    }

    public void setRequiredRoles(List<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }

    public List<String> getRemoteableClasses() {
        return classes;
    }

    public void setRemoteableClasses( List<String> classes ) {
        this.classes = classes;
    }

    public Boolean getLimitSerializationClasses() {
        return limitSerializationClasses;
    }

    public void getLimitSerializationClasses( Boolean limit ) {
        this.limitSerializationClasses = limit;
    }

}