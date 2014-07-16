package org.kie.services.client.documentation;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class Scratch {

    @Test
    public void og() throws Exception  { 
        URL url = new URL("http://192.178.168.1:8080/");
        String restBase = url.toExternalForm() + "business-central/rest/";
        URL baseURL = new URL(restBase);
        System.out.println( baseURL.toExternalForm() );
    }
}
