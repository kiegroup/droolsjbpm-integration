package org.drools.camel.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.dataformat.XStreamDataFormat;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.process.AbortWorkItemCommand;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.command.runtime.process.SignalEventCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.GetObjectCommand;
import org.drools.command.runtime.rule.GetObjectsCommand;
import org.drools.command.runtime.rule.InsertElementsCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.ModifyCommand;
import org.drools.command.runtime.rule.QueryCommand;
import org.drools.command.runtime.rule.RetractCommand;
import org.drools.command.runtime.rule.ModifyCommand.SetterImpl;
import org.drools.common.DefaultFactHandle;
import org.drools.common.DisconnectedFactHandle;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.runtime.rule.impl.FlatQueryResults;
import org.drools.runtime.rule.impl.NativeQueryResults;

public class XStreamHelper {
    public static void setAliases(XStreamDataFormat dataFormat) {
        Map<String, String> map = dataFormat.getAliases();
        if ( map == null ) {
            map = new HashMap<String, String>();
        }

        map.put( "batch-execution",
                 BatchExecutionCommand.class.getName() );
        map.put( "insert",
                 InsertObjectCommand.class.getName() );
        map.put( "modify",
                 ModifyCommand.class.getName() );
        map.put( "setters",
                 SetterImpl.class.getName() );
        map.put( "retract",
                 RetractCommand.class.getName() );
        map.put( "insert-elements",
                 InsertElementsCommand.class.getName() );
        map.put( "start-process",
                 StartProcessCommand.class.getName() );
        map.put( "signal-event",
                 SignalEventCommand.class.getName() );
        map.put( "complete-work-item",
                 CompleteWorkItemCommand.class.getName() );
        map.put( "abort-work-item",
                 AbortWorkItemCommand.class.getName() );
        map.put( "set-global",
                 SetGlobalCommand.class.getName() );
        map.put( "get-global",
                 GetGlobalCommand.class.getName() );
        map.put( "get-object",
                 GetObjectCommand.class.getName() );
        map.put( "get-objects",
                 GetObjectsCommand.class.getName() );
        map.put( "execution-results",
                 ExecutionResultImpl.class.getName() );
        map.put( "fire-all-rules",
                 FireAllRulesCommand.class.getName() );
        map.put( "query",
                 QueryCommand.class.getName() );
        map.put( "query-results",
                 FlatQueryResults.class.getName() );
        map.put( "query-results",
                 NativeQueryResults.class.getName() );
        map.put( "fact-handle",
                 DefaultFactHandle.class.getName() );
        map.put( "fact-handle",
                 DisconnectedFactHandle.class.getName() );

        dataFormat.setAliases( map );
    }
}
