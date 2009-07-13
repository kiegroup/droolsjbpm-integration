package org.drools.simulation;

import org.drools.builder.KnowledgeBuilder;
import org.drools.command.Context;
import org.drools.reteoo.ReteooWorkingMemory;

public interface KnowledgeBaseCommand<T> extends org.drools.command.Command {
	
	T execute(Context context, Object[] args);
	
}
