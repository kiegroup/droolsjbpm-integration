package org.drools;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.drools.client.RuleBaseRepository;
import org.drools.common.InternalRuleBase;

public class RuleBaseRepositoryImpl implements RuleBaseRepository {
    /* @todo: I'm not sure if these need to be LinkedHashMaps, but predictable ordering helps with unit tests :) */
    private Map ruleBases = new LinkedHashMap();
    private Map info = new LinkedHashMap();    
    
    public RuleBaseRepositoryImpl() {
        
    }
    
    /* (non-Javadoc)
     * @see org.drools.RuleBaseRepository#registerRuleBase(org.drools.RuleBase, org.drools.RuleBaseInfo)
     */
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
    
    /* (non-Javadoc)
     * @see org.drools.RuleBaseRepository#deregisterRuleBase(java.lang.String)
     */
    public boolean deregisterRuleBase(String id) {
        InternalRuleBase ruleBase = ( InternalRuleBase ) this.ruleBases.remove( id );
        if ( ruleBase != null ) {
            this.info.remove( id );
        }
        
        return ( ruleBase != null );
    }   
    
    /* (non-Javadoc)
     * @see org.drools.RuleBaseRepository#isRegistered(java.lang.String)
     */
    public boolean isRegistered(String id) {
        InternalRuleBase ruleBase = ( InternalRuleBase ) this.ruleBases.get( id );
        return this.ruleBases.containsKey( ruleBase.getId() );
    }
    
    /* (non-Javadoc)
     * @see org.drools.RuleBaseRepository#listRuleBases()
     */
    public RuleBaseInfo[] listRuleBases() {
        return ( RuleBaseInfo[] ) info.values().toArray( new RuleBaseInfo[ info.size() ] );
    }
       
    /* (non-Javadoc)
     * @see org.drools.RuleBaseRepository#get(org.drools.RuleBaseInfo)
     */
    public RuleBase get(RuleBaseInfo info) {
        //return ( RuleBase ) this.ruleBases.get( info.getId() );
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.drools.RuleBaseRepository#release(org.drools.RuleBaseInfo)
     */
    public void  release(RuleBaseInfo info) {
        //this.ruleBases.get( info.getId() ); 
    }

    public boolean deregisterRuleBase(RuleBase ruleBase) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRegistered(RuleBase ruleBase) {
        // TODO Auto-generated method stub
        return false;
    }    
    
}
