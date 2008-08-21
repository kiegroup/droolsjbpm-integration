package org.drools.server;

import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;

public class TeamAllocationTest extends TestCase {

	public void testBasics() throws Exception {
		PackageBuilder pb = new PackageBuilder();
		pb.addPackageFromDrl(new InputStreamReader(getClass().getResourceAsStream("/TeamAllocation.drl")));
		assertFalse(pb.hasErrors());

		RuleBase rb = RuleBaseFactory.newRuleBase();
		rb.addPackage(pb.getPackage());


	}

}
