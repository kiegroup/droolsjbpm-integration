package org.drools;

import org.drools.client.RuleBaseRepository;

public class DroolsServerImpl implements DroolsServer {
    private RuleBaseRepository ruleBaseRepositoryImpl = new RuleBaseRepositoryImpl();

    public boolean registerRuleBase(RuleBase ruleBase,
                                    RuleBaseInfo info) {        
        return this.ruleBaseRepositoryImpl.registerRuleBase( ruleBase, info );
    }
    
    public boolean deregisterRuleBase(String id) {
        //return this.ruleBaseRepositoryImpl.deregisterRuleBase( id );
        return false;
    }

    public boolean isRegistered(String id) {
        //return this.ruleBaseRepositoryImpl.isRegistered( id );
        return false;
    }

    public RuleBaseInfo[] listRuleBases() {
        //  return this.ruleBaseRepositoryImpl.listRuleBases();
        return null;
    }      
}
