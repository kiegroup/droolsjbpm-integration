package org.drools;

public interface DroolsServer {
    public boolean registerRuleBase(RuleBase ruleBase,
                                    RuleBaseInfo info);
    
    public boolean deregisterRuleBase(String id);
    
    public boolean isRegistered(String id);
    
    public RuleBaseInfo[] listRuleBases();    
    
}
