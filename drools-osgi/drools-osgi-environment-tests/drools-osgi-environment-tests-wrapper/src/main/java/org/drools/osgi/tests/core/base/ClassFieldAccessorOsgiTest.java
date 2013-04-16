package org.drools.osgi.tests.core.base;

import org.drools.core.base.ClassFieldAccessorTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ClassFieldAccessorOsgiTest extends ClassFieldAccessorTest {

}
