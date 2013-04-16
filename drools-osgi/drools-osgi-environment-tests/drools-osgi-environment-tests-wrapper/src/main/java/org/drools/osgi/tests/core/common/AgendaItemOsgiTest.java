package org.drools.osgi.tests.core.common;

import org.drools.core.common.AgendaItemTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class AgendaItemOsgiTest extends AgendaItemTest {

}
