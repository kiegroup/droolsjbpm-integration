package org.drools.osgi.tests.core.base;

import org.drools.core.base.FieldFactoryTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class FieldFactoryOsgiTest extends FieldFactoryTest {

}
