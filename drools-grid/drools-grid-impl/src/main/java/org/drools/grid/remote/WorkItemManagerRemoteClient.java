package org.drools.grid.remote;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.process.AbortWorkItemCommand;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.commands.RegisterWorkItemHandlerRemoteCommand;

import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

 public class WorkItemManagerRemoteClient
    implements
    WorkItemManager,
    Serializable {

    private static final long    serialVersionUID = 1L;

    
    private String               instanceId;
    private ConversationManager  cm;
    private GridServiceDescription<GridNode> gsd;

    public WorkItemManagerRemoteClient(String instanceId, GridServiceDescription gsd, ConversationManager cm) {
        this.instanceId = instanceId;
        this.cm = cm;
        this.gsd = gsd;
    }

    
    
    public void abortWorkItem(long id) {
        String kresultsId = "kresults_" + this.gsd.getId();
        
        CommandImpl cmd = new CommandImpl("execute",
                Arrays.asList(new Object[]{ new KnowledgeContextResolveFromContextCommand( new AbortWorkItemCommand( id ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId )}));
        
         ConversationUtil.sendMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                cmd);
    }

    public void completeWorkItem(long id,
                                 Map<String, Object> results) {
        
        String kresultsId = "kresults_" + this.gsd.getId();
        
        CommandImpl cmd = new CommandImpl("execute",
                Arrays.asList(new Object[]{ new KnowledgeContextResolveFromContextCommand( new CompleteWorkItemCommand( id,
                                                                                                               results ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId )}));
        
         ConversationUtil.sendMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                cmd);
        
        

    }

    public void registerWorkItemHandler(String workItemName,
                                        WorkItemHandler handler) {
        
        String kresultsId = "kresults_" + this.gsd.getId();
        //The work item handler implementation should be in the remote location
        //The work item will be instantiated in the remote location using reflection
        CommandImpl cmd = new CommandImpl("execute",
                Arrays.asList(new Object[]{ new KnowledgeContextResolveFromContextCommand( new RegisterWorkItemHandlerRemoteCommand( workItemName,
                                                                                                               handler.getClass().getCanonicalName() ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId )}));
         
         ConversationUtil.sendMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                cmd);
        
    }

   

}
