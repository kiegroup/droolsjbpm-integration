package org.kie.services.remote.rest;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import javax.ws.rs.Path;

import org.junit.Test;

public class DeploymentIdRegexTest {

    @Test
    public void regextTest() {
        Path pathAnno = DeploymentResource.class.getAnnotation(Path.class);
        String path = pathAnno.value();
        path = path.replace("/deployment/{deploymentId: ", "");
        String regex = path.substring(0, path.length()-1);

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
    }

}
