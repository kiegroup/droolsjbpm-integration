package org.kie.remote.services.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.remote.services.rest.DeployResourceBase.createDeploymentUnit;

import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.junit.Test;
import org.kie.remote.services.rest.api.DeploymentResource;
import org.kie.remote.services.rest.api.RuntimeResource;

public class DeploymentIdTest extends DeploymentResourceImpl {

    @Test
    public void parseDeploymentIdTest() { 
        // Test : groups
        {
        String [] test = { "a", "b", "c" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"), null);
        checkDepUnit(depUnit, test); 
        }
       
        {
        String [] test = { "g", "a", "v", "kbase", "ksess" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"), null);
        checkDepUnit(depUnit, test); 
        }
        
        { 
        String [] test = { "g", "a", "v", "kbase" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"), null);
        checkDepUnit(depUnit, test); 
        }

        {
        String [] test = { "group.sub_group", "artifact_id", "1.0.0.Final" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"), null);
        checkDepUnit(depUnit, test); 
        }
    }
    
    private String join(String [] strArr, String sep) { 
      StringBuilder builder = new StringBuilder();
      if( strArr.length > 0 ) { 
         builder.append(strArr[0]);
         for( int i = 1; i < strArr.length; ++i ) { 
            builder.append( sep + strArr[i]);
         }
      }
      return builder.toString();
    }
    
    private void checkDepUnit(DeploymentUnit depUnit, String [] test) {
       String [] depUnitArr = depUnit.getIdentifier().split(":");
       assertEquals( "size/# components", test.length, depUnitArr.length);
       for( int i = 0; i < depUnitArr.length; ++i ) { 
           assertEquals(test[i], depUnitArr[i]);
       }
    }
}
