package org.drools.client;

import org.drools.RuleBase;
import org.drools.RuleBaseInfo;

public interface RuleBaseRepository {

    public abstract boolean registerRuleBase(RuleBase ruleBase,
                                             RuleBaseInfo info);

    public abstract boolean deregisterRuleBase(RuleBase ruleBase);

    public abstract boolean isRegistered(RuleBase ruleBase);

    public abstract RuleBaseInfo[] listRuleBases();

    public abstract RuleBase get(RuleBaseInfo info);

    public abstract void release(RuleBaseInfo info);

}