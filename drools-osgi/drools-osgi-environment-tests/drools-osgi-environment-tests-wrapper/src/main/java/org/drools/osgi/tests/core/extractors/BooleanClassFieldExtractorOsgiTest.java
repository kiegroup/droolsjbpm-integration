package org.drools.osgi.tests.core.extractors;

import org.drools.core.base.extractors.BooleanClassFieldExtractorTest;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BooleanClassFieldExtractorOsgiTest extends
        BooleanClassFieldExtractorTest {

}
