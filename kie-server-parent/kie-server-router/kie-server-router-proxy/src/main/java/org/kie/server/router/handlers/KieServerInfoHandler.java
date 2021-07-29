/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.handlers;

import java.util.stream.Collectors;

import org.kie.server.router.KieServerRouterEnvironment;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import static org.kie.server.router.KieServerRouterResponsesUtil.buildJAXBServerInfoReponse;
import static org.kie.server.router.KieServerRouterResponsesUtil.buildJSONServerInfoReponse;
import static org.kie.server.router.KieServerRouterResponsesUtil.buildXSTREAMServerInfoReponse;


public class KieServerInfoHandler implements HttpHandler {

    private KieServerRouterEnvironment env;

    public KieServerInfoHandler(KieServerRouterEnvironment env) {
        this.env = env;
    }

    private KieServerRouterEnvironment environment() {
        return env;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestMethod().equals(HttpString.tryFromString("OPTIONS"))) {
            String response = "GET, OPTIONS";
            
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=UTF-8");
            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, response.getBytes("UTF-8").length);
            exchange.getResponseHeaders().put(Headers.ALLOW, response);
            exchange.getResponseSender().send(response);
        }
        HeaderValues accept = exchange.getRequestHeaders().get(Headers.ACCEPT);
        HeaderValues kieContentType = exchange.getRequestHeaders().get("X-KIE-ContentType");
        
        String acceptRequest = "";
        if (accept != null) {
            acceptRequest = accept.stream().collect(Collectors.joining(","));
        }
        
        String kieContentTypeRequest = "";
        if (kieContentType != null) {
            kieContentTypeRequest = kieContentType.stream().collect(Collectors.joining(","));
        }
        
        String response = buildJAXBServerInfoReponse(environment());
        String contentTypeResponse = "application/xml";
        
        if (acceptRequest.toLowerCase().contains("json") || kieContentTypeRequest.toLowerCase().contains("json")) {
            response = buildJSONServerInfoReponse(environment());
            contentTypeResponse = "application/json";
        } else if (kieContentTypeRequest.toLowerCase().contains("xstream")) {
            response = buildXSTREAMServerInfoReponse(environment());
            contentTypeResponse = "application/xml";
        }
        
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentTypeResponse);
        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, response.getBytes("UTF-8").length);
        exchange.getResponseSender().send(response);

    }

}
