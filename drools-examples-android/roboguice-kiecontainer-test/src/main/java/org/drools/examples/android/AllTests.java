package com.cnh.pf.product.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

   public static Test suite() {
      return new TestSuiteBuilder(AllTests.class).includeAllPackagesUnderHere().build();
   }
}
