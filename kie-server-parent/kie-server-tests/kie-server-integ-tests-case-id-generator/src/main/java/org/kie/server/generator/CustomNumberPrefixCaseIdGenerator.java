package org.kie.server.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jbpm.casemgmt.api.generator.CaseIdGenerator;
import org.jbpm.casemgmt.api.generator.CasePrefixNotFoundException;

public class CustomNumberPrefixCaseIdGenerator implements CaseIdGenerator {

    private static final String IDENTIFIER = "CUSTOM_NUMBER_PREFIX";

    private static final String CUSTOM_PREFIX = "01234";

    private static ConcurrentMap<String, AtomicLong> caseIdMap = new ConcurrentHashMap<>();

    @Override
    public String generate(String prefix, Map<String, Object> optionalParameters) throws CasePrefixNotFoundException {
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
}
