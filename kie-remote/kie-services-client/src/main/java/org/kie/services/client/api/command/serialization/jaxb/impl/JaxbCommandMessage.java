package org.kie.services.client.api.command.serialization.jaxb.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.drools.core.command.runtime.rule.InsertElementsCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.ModifyCommand;
import org.drools.core.command.runtime.rule.QueryCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.jbpm.services.task.commands.ActivateTaskCommand;
import org.jbpm.services.task.commands.ClaimNextAvailableTaskCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.DelegateTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.ForwardTaskCommand;
import org.jbpm.services.task.commands.NominateTaskCommand;
import org.jbpm.services.task.commands.ReleaseTaskCommand;
import org.jbpm.services.task.commands.ResumeTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.StopTaskCommand;
import org.jbpm.services.task.commands.SuspendTaskCommand;
import org.kie.api.command.Command;

@XmlRootElement(name="command-message")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbCommandMessage<T> {

    @XmlElement
    @XmlSchemaType(name="string")
    private String deploymentId; 
    
    @XmlElement
    @XmlSchemaType(name="long")
    private String processInstanceId; 
    
	@XmlElement(name="ver")
    @XmlSchemaType(name="int")
    private Integer version; 
    
    @XmlElements({
    	@XmlElement(name = "abort-process-instance", type = AbortProcessInstanceCommand.class),
        @XmlElement(name = "abort-work-item", type = AbortWorkItemCommand.class),
        @XmlElement(name = "complete-work-item", type = CompleteWorkItemCommand.class),
        @XmlElement(name = "delete", type = DeleteCommand.class),
        @XmlElement(name = "fire-all-rules", type = FireAllRulesCommand.class),
        @XmlElement(name = "get-global", type = GetGlobalCommand.class),
        @XmlElement(name = "get-object", type = GetObjectCommand.class),
        @XmlElement(name = "get-objects", type = GetObjectsCommand.class),
        @XmlElement(name = "insert-object", type = InsertObjectCommand.class),
        @XmlElement(name = "insert-elements", type = InsertElementsCommand.class),
        @XmlElement(name = "modify", type = ModifyCommand.class),
        @XmlElement(name = "query", type = QueryCommand.class),
        @XmlElement(name = "set-global", type = SetGlobalCommand.class),
        @XmlElement(name = "signal-event", type = SignalEventCommand.class),
        @XmlElement(name = "start-process", type = StartProcessCommand.class),
        @XmlElement(name = "update", type = UpdateCommand.class),
        
        @XmlElement(name = "activate-task", type = ActivateTaskCommand.class),
        @XmlElement(name = "claim-next-available-task", type = ClaimNextAvailableTaskCommand.class),
        @XmlElement(name = "claim-task", type = ClaimTaskCommand.class),
        @XmlElement(name = "complete-task", type = CompleteTaskCommand.class),
        @XmlElement(name = "delegate-task", type = DelegateTaskCommand.class),
        @XmlElement(name = "exit-task", type = ExitTaskCommand.class),
        @XmlElement(name = "fail-task", type = FailTaskCommand.class),
        @XmlElement(name = "forward-task", type = ForwardTaskCommand.class),
        @XmlElement(name = "nominate-task", type = NominateTaskCommand.class),
        @XmlElement(name = "release-task", type = ReleaseTaskCommand.class),
        @XmlElement(name = "resume-task", type = ResumeTaskCommand.class),
        @XmlElement(name = "skip-task", type = SkipTaskCommand.class),
        @XmlElement(name = "start-task", type = StartTaskCommand.class),
        @XmlElement(name = "stop-task", type = StopTaskCommand.class),
        @XmlElement(name = "suspend-task", type = SuspendTaskCommand.class)
    })
    protected List<Command<T>> commands;    

    public JaxbCommandMessage() { 
        // Default constructor
    }
    
    public JaxbCommandMessage(String deploymentId, int version, Command<T> command) { 
        this.deploymentId = deploymentId;
        this.version = version;
        this.commands = new ArrayList<Command<T>>();
        this.commands.add(command);
     }
     
    public JaxbCommandMessage(String deploymentId, int version, List<Command<T>> commands) { 
       this.deploymentId = deploymentId;
       this.version = version;
       this.commands = commands;
    }
    
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setCommands(List<Command<T>> commands) {
        this.commands = commands;
    }

    public List<Command<T>> getCommands() { 
        return this.commands;
    }
    
}
