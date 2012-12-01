package org.drools.grid.io.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.ContextImplWithEviction;
import org.kie.command.Context;
import org.kie.command.World;

public class NodeData {
    private World contextManager;

    private Context        root;
    private Context        temp;

    public static String   ROOT             = "ROOT";
    public static String   TEMP             = "__TEMP__";
    public static String   NODE_DATA        = "__NodeData__";

    private AtomicInteger  sessionIdCounter = new AtomicInteger();

    public NodeData() {
        // Setup ROOT context, this will hold all long lived intances and instanceIds
        this.contextManager = new ContextManagerImpl();

        this.root = new ContextImpl( ROOT,
                                     this.contextManager );
        ((ContextManagerImpl) this.contextManager).addContext( this.root );
        this.root.set( NODE_DATA,
                       this );
        // Setup TEMP context, this will hold all short lived instanceId and instances
        // TODO: TEMP context should have a time/utilisation eviction queue added 
        this.temp = new ContextImplWithEviction( TEMP,
                                     this.contextManager,
                                     this.root);
        
        ((ContextManagerImpl) this.contextManager).addContext( this.temp );
    }

    public AtomicInteger getSessionIdCounter() {
        return this.sessionIdCounter;
    }

    public World getContextManager() {
        return this.contextManager;
    }

    public void setContextManager(World contextManager) {
        this.contextManager = contextManager;
    }

    public Context getRoot() {
        return this.root;
    }

    public void setRoot(Context root) {
        this.root = root;
    }

    public Context getTemp() {
        return this.temp;
    }

    public void setTemp(Context temp) {
        this.temp = temp;
    }

}
