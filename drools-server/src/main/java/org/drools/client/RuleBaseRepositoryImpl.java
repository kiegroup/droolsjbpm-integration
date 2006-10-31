package org.drools.client;

import org.drools.DroolsClient;
import org.drools.DroolsServer;
import org.drools.RuleBase;
import org.drools.RuleBaseInfo;
import org.drools.common.InternalRuleBase;

public class RuleBaseRepositoryImpl implements RuleBaseRepository {
    
    DroolsServer server;
    
    public RuleBaseRepositoryImpl() {
        
    }
    

    public boolean registerRuleBase(RuleBase ruleBase,
                                    RuleBaseInfo info) {
        return this.server.registerRuleBase( ruleBase, 
                                             info );
    }    

    public boolean deregisterRuleBase(RuleBase ruleBase) {
        return this.server.deregisterRuleBase( ( ( InternalRuleBase ) ruleBase ).getId() );
    }

    public RuleBase get(RuleBaseInfo info) {
        //return this.server
        return null;
    }

    public boolean isRegistered(RuleBase ruleBase) {
        return this.server.isRegistered( ( ( InternalRuleBase ) ruleBase ).getId() );
    }

    public RuleBaseInfo[] listRuleBases() {
        return this.server.listRuleBases();
    }


    public void release(RuleBaseInfo info) {
        
    }

}
