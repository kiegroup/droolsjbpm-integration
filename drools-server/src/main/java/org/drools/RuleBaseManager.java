package org.drools;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.drools.common.InternalRuleBase;

public class RuleBaseManager {
    private Map ruleBases = new LinkedHashMap();
    private Map info = new LinkedHashMap();    
    
    public RuleBaseManager() {
        
    }
    
    public boolean registerRuleBase(RuleBase ruleBase,
                                    RuleBaseInfo info) {        
        
        InternalRuleBase internalRuleBase = ( InternalRuleBase ) ruleBase;
        if ( internalRuleBase.getId().equals( "default" ) ) {
            // We cannot register a RuleBase that has been created without a UUID
            return false;
        }
        info.setId( internalRuleBase.getId() );
        this.ruleBases.put( internalRuleBase.getId(), ruleBase );
        this.info.put(  internalRuleBase.getId(), info );
        return true;
    }
    
    public boolean deregisterRuleBase(String id) {
        InternalRuleBase ruleBase = ( InternalRuleBase ) this.ruleBases.remove( id );
        if ( ruleBase != null ) {
            this.info.remove( id );
        }
        
        return ( ruleBase != null );
    }   
    
    public boolean isRegistered(String id) {
        InternalRuleBase ruleBase = ( InternalRuleBase ) this.ruleBases.get( id );
        return this.ruleBases.containsKey( ruleBase.getId() );
    }
    
    public RuleBaseInfo[] listRuleBases() {
        return ( RuleBaseInfo[] ) info.values().toArray( new RuleBaseInfo[ info.size() ] );
    }
    
}
