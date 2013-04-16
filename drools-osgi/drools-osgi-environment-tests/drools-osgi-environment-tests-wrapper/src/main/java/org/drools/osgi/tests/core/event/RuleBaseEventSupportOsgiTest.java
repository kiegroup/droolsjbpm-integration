package org.drools.osgi.tests.core.event;

import org.drools.core.event.RuleBaseEventSupportTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class RuleBaseEventSupportOsgiTest extends RuleBaseEventSupportTest {

}
