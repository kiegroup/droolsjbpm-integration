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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import io.undertow.util.Headers;
import org.jboss.logging.Logger;
import org.kie.server.router.KieServerRouterConstants;

public class HttpUtils {

    private static final Logger log = Logger.getLogger(HttpUtils.class);

    private static final String USER_NAME = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_USER, "kieserver");
    private static final String PASSWORD = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_PASSWORD, "kieserver1!");
    private static final String TOKEN = System.getProperty(KieServerRouterConstants.KIE_CONTROLLER_TOKEN);

    public static void deleteHttpCall(String url) throws Exception {

        URL controllerURL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
        con.setRequestMethod("DELETE");

        con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
        con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
        con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization());

        con.setDoOutput(true);

        log.debugf("Sending 'POST' request to URL : %s", controllerURL);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);

    }

    public static String putHttpCall(String url, String body) throws Exception {

        URL controllerURL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) controllerURL.openConnection();
        con.setRequestMethod("PUT");

        con.setRequestProperty(Headers.ACCEPT_STRING, "application/json");
        con.setRequestProperty(Headers.CONTENT_TYPE_STRING, "application/json");
        con.setRequestProperty(Headers.AUTHORIZATION_STRING, getAuthorization());

        con.setDoOutput(true);
        if (body != null) {
            con.getOutputStream().write(body.getBytes("UTF-8"));
        }

        log.debugf("Sending 'POST' request to URL : %s", controllerURL);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        in.close();

        return response.toString();
    }


    protected static String getAuthorization() throws Exception{
        if (TOKEN != null) {
            return "Bearer " + TOKEN;
        } else {
            return "Basic " + Base64.getEncoder().encodeToString((USER_NAME + ":" + PASSWORD).getBytes("UTF-8"));
        }
    }
}
