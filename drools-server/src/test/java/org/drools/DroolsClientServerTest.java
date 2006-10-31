package org.drools;

import org.drools.common.InternalRuleBase;

import junit.framework.TestCase;

public class DroolsClientServerTest extends TestCase {
    private DroolsClient client;
    private DroolsService service;
    
//    protected void setUp() throws Exception {
//        this.service = new DroolsService();
//        this.service.start();
//        
//        client = new DroolsClient();
//        this.client.start();        
//        Thread.sleep( 1000 );
//    }
//
//    public void test1() throws Exception {
//        DroolsServer server = client.connect();
//        
//        InternalRuleBase ruleBase1 = ( InternalRuleBase ) RuleBaseFactory.newRuleBase();               
//        RuleBaseInfo info1 = new RuleBaseInfo();
//        info1.setName( "test 1" );                
//        server.registerRuleBase( ruleBase1, info1 );
//        
//        InternalRuleBase ruleBase2 = ( InternalRuleBase ) RuleBaseFactory.newRuleBase();               
//        RuleBaseInfo info2 = new RuleBaseInfo();
//        info2.setName( "test 2" );                
//        server.registerRuleBase( ruleBase2, info2 );        
//        
//        RuleBaseInfo[] infos = server.listRuleBases();
//        
//        assertEquals( 2, infos.length );
//        
//        assertNotSame( info1, infos[0] );
//        assertNotSame( info2, infos[1] );
//        assertNotSame( infos[1], infos[0] );
//                
//        info1 = infos[0];
//        assertEquals(info1.getId(), ruleBase1.getId());
//        assertEquals(info1.getName(), "test 1");
//
//        info2 = infos[1];
//        assertEquals(info2.getId(), ruleBase2.getId());
//        assertEquals(info2.getName(), "test 2");        
//        
//        client.disconnect( server );
//    }
//    
//    protected void tearDown() throws Exception {
//        this.client.stop();
//        this.service.exit();
//    }
}
