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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.kie.server.router.proxy.aggragate.ResponseAggregator;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;


public abstract class AbstractAggregateHttpHandler implements HttpHandler {

    private static final Logger log = Logger.getLogger(AbstractAggregateHttpHandler.class);

    private static final String REPLACE_PAGE =      "page=[^&]*";
    private static final String REPLACE_PAGE_SIZE = "pageSize=[^&]*";

    private static final String DEFAULT_ACCEPT = "application/xml";

    private HttpHandler httpHandler;
    private AdminHttpHandler adminHandler;

    private RoundRobinHostSelector selector = new RoundRobinHostSelector();

    public AbstractAggregateHttpHandler(HttpHandler httpHandler, AdminHttpHandler adminHandler) {
        this.httpHandler = httpHandler;
        this.adminHandler = adminHandler;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if (!exchange.getRequestMethod().equals(HttpString.tryFromString("GET"))) {
            httpHandler.handleRequest(exchange);
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
            }

            return response;
        })
                .filter(msg -> msg != null && !msg.trim().isEmpty())
                .collect(Collectors.toList());

        HeaderValues accept = exchange.getRequestHeaders().get(Headers.ACCEPT);
        HeaderValues kieContentType = exchange.getRequestHeaders().get("X-KIE-ContentType");


        ResponseAggregator responseAggregator = adminHandler.getAggregators().stream().filter(a -> a.supports(kieContentType, accept, DEFAULT_ACCEPT)).findFirst().orElseThrow(() ->
                        new RuntimeException("not possible to find response aggregator for " + responseHeaders.get(Headers.ACCEPT))
        );

        String response = null;
        if (supportAdvancedAggregate()) {
            response = responseAggregator.aggregate(returnResponses, sortBy, sortOder, page, pageSize);
        } else {
            response = responseAggregator.aggregate(returnResponses);
        }

        responseHeaders.forEach((name, value) -> {
            exchange.getResponseHeaders().putAll(HttpString.tryFromString(name), value);
        });
        if (response == null) {
            ResponseCodeHandler.HANDLE_404.handleRequest(exchange);
            return;
        }

        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, response.getBytes("UTF-8").length);
        exchange.getResponseSender().send(response);
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

    protected Set<String> getServerHosts() {

        return adminHandler.getHostsPerServer().values().stream().map(hosts -> {
            return selector.selectHost(hosts.toArray(new String[hosts.size()]));
        })
                .collect(Collectors.toSet());
    }

    protected boolean supportAdvancedAggregate() {
        return true;
    }

    static class RoundRobinHostSelector {

        private final AtomicInteger currentHost = new AtomicInteger(0);


        public String selectHost(String[] availableHosts) {
            int hostIndex = currentHost.incrementAndGet() % availableHosts.length;

            return availableHosts[hostIndex];
        }
    }
}

