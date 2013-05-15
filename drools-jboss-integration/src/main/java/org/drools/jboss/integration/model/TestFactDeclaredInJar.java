package org.drools.jboss.integration.model;

/**
 * Dummy fact used for testing usage of facts declared as a java class and
 * compiled into KIE jar.
 */
public class TestFactDeclaredInJar {

    private final String value;

    public TestFactDeclaredInJar() {
        this.value = null;
    }
    
    public TestFactDeclaredInJar(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
