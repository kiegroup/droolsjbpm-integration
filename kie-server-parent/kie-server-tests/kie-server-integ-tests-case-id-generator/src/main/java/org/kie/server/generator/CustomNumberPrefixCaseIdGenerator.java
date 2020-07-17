/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jbpm.casemgmt.api.generator.CaseIdGenerator;
import org.jbpm.casemgmt.api.generator.CasePrefixCannotBeGeneratedException;
import org.jbpm.casemgmt.api.generator.CasePrefixNotFoundException;
import org.mvel2.CompileException;
import org.mvel2.templates.TemplateRuntime;

import static org.kie.server.generator.CustomCaseIdExpressionFunctions.CASE_ID_FUNCTIONS;

public class CustomNumberPrefixCaseIdGenerator implements CaseIdGenerator {

    private static final String IDENTIFIER = "CUSTOM_NUMBER_PREFIX";

    private static final String CUSTOM_PREFIX = "01234";

    private static ConcurrentMap<String, AtomicLong> caseIdMap = new ConcurrentHashMap<>();

    @Override
    public String generate(String prefix, Map<String, Object> optionalParameters) {
        if(!caseIdMap.containsKey(prefix)) {
            throw new CasePrefixNotFoundException("Case id prefix \"" + prefix + "\" not found.");
        }
        
        long nextVal = caseIdMap.get(prefix).incrementAndGet();
        String paddedNumber = String.format("%05d", nextVal);
        return prefix + "-" + CUSTOM_PREFIX + paddedNumber;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void register(String prefix) {
        caseIdMap.putIfAbsent(prefix, new AtomicLong(0L));
    }

    @Override
    public void unregister(String prefix) {
        caseIdMap.remove(prefix);
    }
    
    @Override
    public String resolveCaseIdPrefix(String expression, Map<String, Object> optionalParameters) {
        try {
            return !expression.isEmpty() ? (String) TemplateRuntime.eval(expression, CASE_ID_FUNCTIONS, optionalParameters) : "";
        } catch (CompileException e) {
            throw new CasePrefixCannotBeGeneratedException("Case Id Prefix cannot be generated", e);
        }
    }
}
