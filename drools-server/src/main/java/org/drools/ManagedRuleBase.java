package org.drools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.drools.rule.Package;

public class ManagedRuleBase
    implements
    RuleBase {    
    RuleBase ruleBase;
               
    public ManagedRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    public void addPackage(Package pkg) throws Exception {
        this.ruleBase.addPackage( pkg );
    }

    public Package[] getPackages() {
        return this.ruleBase.getPackages();
    }

    public Set getWorkingMemories() {
        return this.ruleBase.getWorkingMemories();
    }

    public WorkingMemory newWorkingMemory() {
        return this.ruleBase.newWorkingMemory();
    }

    public WorkingMemory newWorkingMemory(boolean keepReference ) {
        return this.ruleBase.newWorkingMemory( keepReference );
    }

    public WorkingMemory newWorkingMemory(InputStream stream) throws IOException,
                                                           ClassNotFoundException {
        return this.ruleBase.newWorkingMemory( stream );        
    }

    public WorkingMemory newWorkingMemory(InputStream stream,
                                          boolean keepReference) throws IOException,
                                                       ClassNotFoundException {
        return this.newWorkingMemory( stream,
                                      keepReference );
    }

    public void removePackage(String packageName) {
        this.ruleBase.removePackage( packageName );
    }

    public void removeRule(String packageName,
                           String ruleName) {
        this.ruleBase.removeRule( packageName,
                                  ruleName );
    }

}
