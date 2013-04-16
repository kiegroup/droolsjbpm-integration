package org.drools.osgi.tests.core.mvel;

import org.drools.core.base.mvel.MVELCalendarCoercionTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class MVELCalendarCoercionOsgiTest extends MVELCalendarCoercionTest {

}
