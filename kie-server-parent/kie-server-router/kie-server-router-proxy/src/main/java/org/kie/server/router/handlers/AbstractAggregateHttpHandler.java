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
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.kie.server.router.ConfigurationManager;
import org.kie.server.router.proxy.aggragate.ResponseAggregator;
import org.kie.server.router.utils.MediaTypeUtil;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;


public abstract class AbstractAggregateHttpHandler implements HttpHandler {

    protected static final Logger log = Logger.getLogger(AbstractAggregateHttpHandler.class);

    protected static final String REPLACE_PAGE =      "page=[^&]*";
    protected static final String REPLACE_PAGE_SIZE = "pageSize=[^&]*";

    protected static final String DEFAULT_ACCEPT = "application/xml";

    protected HttpHandler httpHandler;
    protected ConfigurationManager configurationManager;

    private RoundRobinHostSelector selector = new RoundRobinHostSelector();

    public AbstractAggregateHttpHandler(HttpHandler httpHandler, ConfigurationManager configurationManager) {
        this.httpHandler = httpHandler;
        this.configurationManager = configurationManager;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {  
        if (exchange.getRequestMethod().equals(HttpString.tryFromString("OPTIONS"))) {
            handleOptions(exchange);
            return;
        }
        if (!exchange.getRequestMethod().equals(HttpString.tryFromString("GET"))) {
            httpHandler.handleRequest(exchange);
            return;
        }
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


        final Map<String,List<String>> responseHeaders = new ConcurrentHashMap<>();
        List<String> returnResponses = getServerHosts().parallelStream().map(url -> {
            String response = null;
            try {
                response = sendRequest(url, exchange, responseHeaders, routerPage, routerPageSize);
            } catch (Exception e) {
                log.error("Error when forwarding request to server", e);

                removeHostOnException(url, e);
            }

            return response;
        })
                .filter(msg -> msg != null && !msg.trim().isEmpty())
                .collect(Collectors.toList());

        if (returnResponses.isEmpty()) {
            ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
            return;
        }

        HeaderValues accept = exchange.getRequestHeaders().get(Headers.ACCEPT);
        HeaderValues kieContentType = exchange.getRequestHeaders().get("X-KIE-ContentType");

        ResponseAggregator responseAggregator = configurationManager.getConfiguration().getAggregators().stream().filter(a -> a.supports(kieContentType, accept, DEFAULT_ACCEPT)).findFirst().orElseThrow(() ->
                        new RuntimeException("not possible to find response aggregator for " + responseHeaders.get(Headers.ACCEPT))
        );

        // we should presume the headers are coming are the same type
        // if the media type comes from the server we just get one of them (the first)
        boolean aggregatable = isAggregatable(responseHeaders);
        if (!aggregatable) {
            returnResponses = Collections.singletonList(returnResponses.get(0));
        }

        responseHeaders.forEach((name, value) -> {
            exchange.getResponseHeaders().putAll(HttpString.tryFromString(name), value);
        });

        String response = null;
        if (supportAdvancedAggregate()) {
            response = responseAggregator.aggregate(returnResponses, sortBy, sortOder, page, pageSize);
        } else {
            response = responseAggregator.aggregate(returnResponses);
        }



        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, response.getBytes("UTF-8").length);
        exchange.getResponseSender().send(response);
    }

    private boolean isAggregatable(Map<String, List<String>> responseHeaders) {
        List<String> type = responseHeaders.get(Headers.CONTENT_TYPE_STRING);

        // we don't know the type so we don't aggregate
        if (type == null || type.isEmpty()) {
            return true;
        }

        Map<String, String> parameters = MediaTypeUtil.extractParameterFromMediaTypeString(type.get(0));
        if (parameters.containsKey("aggregatable")) {
            Boolean aggregate = Boolean.parseBoolean(parameters.get("aggregatable"));
            return (aggregate != null && aggregate);
        }

        return true;
    }

    protected String sendRequest(String url, HttpServerExchange exchange, Map<String,List<String>> responseHeaders, String page, String pageSize) throws Exception {

        URL obj = new URL(url + exchange.getRequestPath() + "?" + exchange.getQueryString().replaceAll(REPLACE_PAGE, "page=" + page).replaceAll(REPLACE_PAGE_SIZE, "pageSize="+pageSize));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        //add request headers
        exchange.getRequestHeaders().forEach(h -> {
            con.setRequestProperty(h.getHeaderName().toString(), h.getFirst());
        });

        log.debugf("Sending 'GET' request to URL : %s", obj);
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
    
    protected String sendOptionsRequest(String url, HttpServerExchange exchange, Map<String,List<String>> responseHeaders) throws Exception {

        URL obj = new URL(url + exchange.getRequestPath() + "?" + exchange.getQueryString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("OPTIONS");

        //add request headers
        exchange.getRequestHeaders().forEach(h -> {
            con.setRequestProperty(h.getHeaderName().toString(), h.getFirst());
        });

        log.debugf("Sending 'OPTIONS' request to URL : %s", obj);
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

    protected Set<String> getServerHosts() {
        return configurationManager.getConfiguration().getHostsPerServer().values().stream().map(hosts -> {
            Set<String> uniqueHosts = new LinkedHashSet<>(hosts);
            return selector.selectHost(uniqueHosts.toArray(new String[uniqueHosts.size()]));
        }).filter(host -> host != null)
         .collect(Collectors.toSet());
    }

    protected void removeHostOnException(String url, Exception e) {
        if (e instanceof SocketException || e instanceof UnknownHostException) {
            configurationManager.disconnectFailedHost(url);
            log.warn("Removed host '" + url + "' due to its unavailability (cause " + e.getMessage() + ")");
        }
    }

    protected boolean supportAdvancedAggregate() {
        return true;
    }

    static class RoundRobinHostSelector {

        private final AtomicInteger currentHost = new AtomicInteger(0);


        public String selectHost(String[] availableHosts) {
            if (availableHosts.length == 0) {
                return null;
            }
            int hostIndex = currentHost.incrementAndGet() % availableHosts.length;

            return availableHosts[hostIndex];
        }
    }
    
    protected void handleOptions(HttpServerExchange exchange) throws Exception {
        final Map<String,List<String>> responseHeaders = new ConcurrentHashMap<>();
        String returnResponse = getServerHosts().stream().findFirst().map(url -> {
            String response = null;
            try {
                response = sendOptionsRequest(url, exchange, responseHeaders);
            } catch (Exception e) {
                log.error("Error when forwarding request to server", e);

                removeHostOnException(url, e);
            }

            return response;
        })
                .filter(msg -> msg != null && !msg.trim().isEmpty())
                .orElse(null);

        if (returnResponse == null) {
            ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
            return;
        }
        responseHeaders.forEach((name, value) -> {
            exchange.getResponseHeaders().putAll(HttpString.tryFromString(name), value);
        });

        exchange.getResponseHeaders().put(Headers.ALLOW, returnResponse);
        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, returnResponse.getBytes("UTF-8").length);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=UTF-8");
        exchange.getResponseSender().send(returnResponse);
    }
}

