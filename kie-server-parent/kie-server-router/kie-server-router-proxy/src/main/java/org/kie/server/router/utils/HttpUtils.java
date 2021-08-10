/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.kie.server.router.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.jboss.logging.Logger;
import org.kie.server.router.KieServerRouterEnvironment;

import io.undertow.util.Headers;

public class HttpUtils {

    private static final Logger log = Logger.getLogger(HttpUtils.class);


    public static void deleteHttpCall(KieServerRouterEnvironment env, String url) throws Exception {

        URL controllerURL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
        con.setRequestMethod("DELETE");

        con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
        con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
        con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization(env));

        con.setDoOutput(true);

        log.debugf("Sending 'DELETE' request to URL : %s", controllerURL);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);
        if (responseCode > 204) {
            throw new IOException("Unsucessful response code " + responseCode);
        }

    }
    
    public static void getHttpCall(KieServerRouterEnvironment env, String url) throws Exception {

        URL controllerURL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
        con.setRequestMethod("GET");

        con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
        con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
        con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization(env));

        con.setDoOutput(true);

        log.debugf("Sending 'GET' request to URL : %s", controllerURL);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);
        
        if (responseCode != 200) {
            throw new IOException("Unsucessful response code " + responseCode);
        }

    }

    public static String putHttpCall(KieServerRouterEnvironment env, String url, String body) throws Exception {

        URL controllerURL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
        con.setRequestMethod("PUT");

        con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
        con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
        con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization(env));

        con.setDoOutput(true);
        if (body != null) {
            con.getOutputStream().write(body.getBytes("UTF-8"));
        }

        log.debugf("Sending 'PUT' request to URL : %s", controllerURL);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);
        if (responseCode > 201) {
            throw new IOException("Unsucessful response code " + responseCode);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        in.close();

        return response.toString();
    }


    protected static String getAuthorization(KieServerRouterEnvironment env) throws Exception{
        if (env.hasKieControllerToken()) {
            return "Bearer " + env.getKieControllerToken();
        } else {
            return "Basic " + Base64.getEncoder().encodeToString((env.getKieControllerUser() + ":" + env.getKieControllerPwd()).getBytes("UTF-8"));
        }
    }
}
