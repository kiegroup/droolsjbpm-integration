package org.kie.services.remote.rest;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.junit.Test;
import org.kie.internal.deployment.DeploymentUnit;

public class DeploymentIdTest extends DeploymentResource {

    @Test
    public void sameRegexUsedEverywhereTest() {
        Path pathAnno = RuntimeResource.class.getAnnotation(Path.class);
        String path = pathAnno.value();
        path = path.replace("/runtime/{deploymentId: ", "");
        String runRegex = path.substring(0, path.length()-1);

        pathAnno = DeploymentResource.class.getAnnotation(Path.class);
        path = pathAnno.value();
        path = path.replace("/deployment/{deploymentId: ", "");
        String depRegex = path.substring(0, path.length()-1);
        
        assertEquals("DeploymentResource deploymentId regex does not match RuntimeResource's", depRegex, runRegex);
    }

    @Test
    public void deploymentIdRegexTest() {
        Path pathAnno = DeploymentResource.class.getAnnotation(Path.class);
        String path = pathAnno.value();
        path = path.replace("/deployment/{deploymentId: ", "");
        String regex = path.substring(0, path.length()-1);

        // Test : groups
        String test = "a:b:c";
        assertTrue(test, Pattern.matches(regex, test));
        test = "a:b:c:d";
        assertTrue(test, Pattern.matches(regex, test));
        
        test = "g:a:v:kbase:ksess:";
        assertFalse(test, Pattern.matches(regex, test));
        test = "g:a:v:kbase:ksess:a:";
        assertFalse(test, Pattern.matches(regex, test));
        test = "g:a:v:kbase:kse ss";
        assertFalse(test, Pattern.matches(regex, test));
        test = "g:a :v:kbase:kse ss";
        assertFalse(test, Pattern.matches(regex, test));
        
        test = "g:a:v:kbase";
        assertTrue(test, Pattern.matches(regex, test));
        test = "g:a:v:kbase:";
        assertTrue(test, Pattern.matches(regex, test));
        test = "g:a:v::ksess";
        assertTrue(test, Pattern.matches(regex, test));
        test = "g:a:v::";
        assertTrue(test, Pattern.matches(regex, test));

        String all = "g:a:v:kbase:ksess";
        assertTrue(test, Pattern.matches(regex, all));
        String[] groups = all.split(":");
        assertEquals(5, groups.length);

        // Test use of "-", "." and "_"
        test = "group.subgroup:artifact-id:1.0";
        assertTrue(test, Pattern.matches(regex, test));
        test = "group.sub_group:artifact_id:1.0.0.Final";
        assertTrue(test, Pattern.matches(regex, test));
        
        test = "group.subgroup:artifact-id:1.0";
        assertTrue(test, Pattern.matches(regex, test));
        test = "group.sub_group:artifact_id:v1.0.0.Final";
        assertTrue(test, Pattern.matches(regex, test));
       
        // Test other
        test = "::v:kbase";
        assertFalse(test, Pattern.matches(regex, test));
        test = "::v";
        assertFalse(test, Pattern.matches(regex, test));
        test = "$:#:v";
        assertFalse(test, Pattern.matches(regex, test));
    }

    @Test
    public void parseDeploymentIdTest() { 
        // Test : groups
        {
        String [] test = { "a", "b", "c" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"));
        checkDepUnit(depUnit, test); 
        }
       
        {
        String [] test = { "g", "a", "v", "kbase", "ksess" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"));
        checkDepUnit(depUnit, test); 
        }
        
        { 
        String [] test = { "g", "a", "v", "kbase" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"));
        checkDepUnit(depUnit, test); 
        }

        {
        String [] test = { "group.sub_group", "artifact_id", "1.0.0.Final" };
        KModuleDeploymentUnit depUnit = createDeploymentUnit(join(test, ":"));
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
