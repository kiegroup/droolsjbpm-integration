package org.drools.osgi.tests.core.base;

import org.drools.core.base.FieldFactoryTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class FieldFactoryOsgiTest extends FieldFactoryTest {

}
