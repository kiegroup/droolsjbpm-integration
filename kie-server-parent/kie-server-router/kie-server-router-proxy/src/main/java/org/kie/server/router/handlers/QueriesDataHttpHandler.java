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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.kie.server.router.proxy.aggragate.ResponseAggregator;

public class QueriesDataHttpHandler extends AbstractAggregateHttpHandler {

    public QueriesDataHttpHandler(HttpHandler httpHandler, AdminHttpHandler adminHandler) {
        super(httpHandler, adminHandler);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestMethod().equals(HttpString.tryFromString("POST"))) {

            Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
            // collect and alter paging
            Integer page = 0;
            Integer pageSize = 10;


            Deque<String> originalPage = queryParams.get("page");
            if (originalPage != null && !originalPage.isEmpty()) {
                page = Integer.parseInt(originalPage.getFirst());
            }
            Deque<String> originalPageSize = queryParams.remove("pageSize");
            if (originalPageSize != null && !originalPageSize.isEmpty()) {
                pageSize = Integer.parseInt(originalPageSize.getFirst());
            }
            boolean useAdvanced = pageSize.intValue() != -1;

            final String routerPage = "0";
            // need to add 1 to page for proper size of page
            final String routerPageSize = String.valueOf((1 + page) * pageSize);

            // collect sorting
            String sortBy = null;
            boolean sortOder = true;
            Deque<String> originalSortBy = queryParams.get("sort");
            if (originalSortBy != null && !originalSortBy.isEmpty()) {
                sortBy = originalSortBy.getFirst();
            }
            Deque<String> originalSortOrder = queryParams.get("sortOrder");
            if (originalSortOrder != null && !originalSortOrder.isEmpty()) {
                sortOder = Boolean.parseBoolean(originalSortOrder.getFirst());
            }


            StringBuilder requestBody = new StringBuilder();
            // collect body of the request
            exchange.getRequestReceiver().receiveFullString((ex, data) -> {
                requestBody.append(data);
            });

            final String body = requestBody.toString();

            final Map<String,List<String>> responseHeaders = new ConcurrentHashMap<>();
            List<String> returnResponses = getServerHosts().parallelStream().map(url -> {
                String response = null;
                try {
                    response = sendPostRequest(url, body, exchange, responseHeaders, routerPage, routerPageSize);
                } catch (Exception e) {
                    log.error("Error when forwarding request to server", e);
                }

                return response;
            })
            .filter(msg -> msg != null && !msg.trim().isEmpty())
            .collect(Collectors.toList());

            HeaderValues accept = exchange.getRequestHeaders().get(Headers.ACCEPT);
            HeaderValues kieContentType = exchange.getRequestHeaders().get("X-KIE-ContentType");

            String response = "";

            if (returnResponses.size() > 0) {
                ResponseAggregator responseAggregator = adminHandler.getAggregators().stream().filter(a -> a.supports(kieContentType, accept, DEFAULT_ACCEPT)).findFirst().orElseThrow(() ->
                                new RuntimeException("not possible to find response aggregator for " + responseHeaders.get(Headers.ACCEPT))
                );

                if (supportAdvancedAggregate() && useAdvanced) {
                    response = responseAggregator.aggregate(returnResponses, sortBy, sortOder, page, pageSize);
                } else {
                    response = responseAggregator.aggregate(returnResponses);
                }
            }
            responseHeaders.forEach((name, value) -> {
                exchange.getResponseHeaders().putAll(HttpString.tryFromString(name), value);
            });

            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, response.getBytes("UTF-8").length);
            exchange.getResponseSender().send(response);


        } else if (exchange.getRequestMethod().equals(HttpString.tryFromString("PUT"))) {

            StringBuilder requestBody = new StringBuilder();
            // collect body of the request
            exchange.getRequestReceiver().receiveFullString((ex, data) -> {
                requestBody.append(data);
            });

            final String body = requestBody.toString();

            final Map<String,List<String>> responseHeaders = new ConcurrentHashMap<>();
            List<String> returnResponses = getServerHosts().parallelStream().map(url -> {
                String response = null;
                try {
                    response = sendPutRequest(url, body, exchange, responseHeaders);
                } catch (Exception e) {
                    log.error("Error when forwarding request to server", e);
                }

                return response;
            })
                    .filter(msg -> msg != null)
                    .collect(Collectors.toList());

            responseHeaders.forEach((name, value) -> {
                exchange.getResponseHeaders().putAll(HttpString.tryFromString(name), value);
            });

            if (returnResponses.size() == 0) {
                ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
                return;
            }
            new ResponseCodeHandler(201).handleRequest(exchange);


        }  else if (exchange.getRequestMethod().equals(HttpString.tryFromString("DELETE"))) {

            final Map<String,List<String>> responseHeaders = new ConcurrentHashMap<>();
            getServerHosts().parallelStream().forEach(url -> {

                try {
                    sendDeleteRequest(url, exchange, responseHeaders);
                } catch (Exception e) {
                    log.error("Error when forwarding request to server", e);
                }


            });

            responseHeaders.forEach((name, value) -> {
                exchange.getResponseHeaders().putAll(HttpString.tryFromString(name), value);
            });

            new ResponseCodeHandler(204).handleRequest(exchange);
            return;



        } else {

            super.handleRequest(exchange);
        }
    }

    protected String sendPostRequest(String url, String body, HttpServerExchange exchange, Map<String,List<String>> responseHeaders, String page, String pageSize) throws Exception {

        URL obj = new URL(url + exchange.getRequestPath() + "?" + exchange.getQueryString().replaceAll(REPLACE_PAGE, "page=" + page).replaceAll(REPLACE_PAGE_SIZE, "pageSize=" + pageSize));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");

        //add request headers
        exchange.getRequestHeaders().forEach(h -> {
            con.setRequestProperty(h.getHeaderName().toString(), h.getFirst());
        });

        con.setDoOutput(true);
        if (body != null) {
            con.getOutputStream().write(body.getBytes("UTF-8"));
        }

        log.debugf("Sending 'POST' request to URL : %s", obj);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);

        Map<String, List<String>> headers = con.getHeaderFields();
        headers.forEach((k, v) -> {
            if (k != null) {
                responseHeaders.put(k, v);
            }
        });

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        in.close();

        return response.toString();
    }

    protected String sendPutRequest(String url, String body, HttpServerExchange exchange, Map<String,List<String>> responseHeaders) throws Exception {

        URL obj = new URL(url + exchange.getRequestPath() + "?" + exchange.getQueryString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("PUT");

        //add request headers
        exchange.getRequestHeaders().forEach(h -> {
            con.setRequestProperty(h.getHeaderName().toString(), h.getFirst());
        });

        con.setDoOutput(true);
        if (body != null) {
            con.getOutputStream().write(body.getBytes("UTF-8"));
        }

        log.debugf("Sending 'PUT' request to URL : %s", obj);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);

        Map<String, List<String>> headers = con.getHeaderFields();
        headers.forEach((k, v) -> {
            if (k != null) {
                responseHeaders.put(k, v);
            }
        });

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        in.close();
        return response.toString();
    }

    protected String sendDeleteRequest(String url, HttpServerExchange exchange, Map<String,List<String>> responseHeaders) throws Exception {

        URL obj = new URL(url + exchange.getRequestPath() + "?" + exchange.getQueryString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("DELETE");

        //add request headers
        exchange.getRequestHeaders().forEach(h -> {
            con.setRequestProperty(h.getHeaderName().toString(), h.getFirst());
        });

        con.setDoOutput(true);

        log.debugf("Sending 'DELETE' request to URL : %s", obj);
        int responseCode = con.getResponseCode();
        log.debugf("Response Code : %s", responseCode);

        Map<String, List<String>> headers = con.getHeaderFields();
        headers.forEach((k, v) -> {
            if (k != null) {
                responseHeaders.put(k, v);
            }
        });

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);

        }
        in.close();

        return response.toString();
    }

}
