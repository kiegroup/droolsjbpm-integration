package org.kie.remote.services.rest.jaxb;

import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;

import org.junit.Test;
import org.kie.remote.services.rest.jaxb.user.AClass;
import org.kie.remote.services.rest.jaxb.user.BClass;
import org.kie.remote.services.rest.jaxb.user.CClass;
import org.kie.remote.services.rest.jaxb.user.DClass;
import org.kie.remote.services.rest.jaxb.user.EClass;

import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;

public class UserClassSerialziationAnnotationUseTest {

    // see 
    @Test
    public void testDuplicateClassesDifferentPackages() throws Exception { 
      
        Class [] duplicateAClasses = { AClass.class, org.kie.remote.services.rest.jaxb.user.sub.AClass.class };
       
        boolean iaeThrown = false;
        JAXBContext jaxbContext;
        try { 
            // No @XmlType(name=) or @XmlType(namespace=) used, and the classes have the same name!
            jaxbContext = JAXBContext.newInstance(duplicateAClasses);
        } catch( IllegalAnnotationsException iae ) { 
            iaeThrown = true;
        }
        assertTrue( "A IllegalAnnotationsException was expected!", iaeThrown);
        
        Class [] dupBClassesWithDiffNamespace = { BClass.class, org.kie.remote.services.rest.jaxb.user.sub.BClass.class };
        // a different namespace does work
        jaxbContext = JAXBContext.newInstance(dupBClassesWithDiffNamespace);
        
        Class [] dupCClassesWithDiffName = { CClass.class, org.kie.remote.services.rest.jaxb.user.sub.CClass.class };
        // a different name does work
        jaxbContext = JAXBContext.newInstance(dupCClassesWithDiffName);
        
        Class [] dClassWithGetterAndAttr = { DClass.class };
        iaeThrown = false;
        try { 
            // having a *public* field and a getter with the same name will cause problems!
            jaxbContext = JAXBContext.newInstance(dClassWithGetterAndAttr);
        } catch( IllegalAnnotationsException iae ) {
            iaeThrown = true;
        }
        assertTrue( "A IllegalAnnotationsException was expected!", iaeThrown);
        
        Class [] eClassWithXmlAccessorType = { EClass.class };
        // but adding an @XmlAccessorType fixes that!
        jaxbContext = JAXBContext.newInstance(eClassWithXmlAccessorType);
    }
}
