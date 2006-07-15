package org.drools;

public class DroolsServerImpl implements DroolsServer {
    private RuleBaseManager ruleBaseManager = new RuleBaseManager();

    public boolean registerRuleBase(RuleBase ruleBase,
                                    RuleBaseInfo info) {        
        return this.ruleBaseManager.registerRuleBase( ruleBase, info );
    }
    
    public boolean deregisterRuleBase(String id) {
        return this.ruleBaseManager.deregisterRuleBase( id );
    }

    public boolean isRegistered(String id) {
        return this.ruleBaseManager.isRegistered( id );
    }

    public RuleBaseInfo[] listRuleBases() {
        return this.ruleBaseManager.listRuleBases();
    }      
}
