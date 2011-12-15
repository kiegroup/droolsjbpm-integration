package org.drools.fluent;

import org.drools.fluent.test.ReflectiveMatcher;
import org.drools.fluent.test.ReflectiveMatcherAssert;

public interface FluentTest<P> {
    <T> P test(java.lang.String reason, T actual, org.hamcrest.Matcher<T> matcher);
    
    <T> P test(T actual, org.hamcrest.Matcher<T> matcher);
    
    <T> P test(String text);
    
    <T> P test(ReflectiveMatcherAssert matcher);
}