/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.impl.security.adapters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class KeycloakSecurityFilter implements Filter {

    static final ThreadLocal<HttpServletRequest> requests = new ThreadLocal<HttpServletRequest>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        requests.set((HttpServletRequest) request);
        try {
            chain.doFilter(request, response);
        } finally {
            requests.remove();
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * Returns the current servlet request that this thread is handling, or null if this thread is not currently handling
     * a servlet request.
     */
    public static HttpServletRequest getRequest() {
        return requests.get();
    }
}
