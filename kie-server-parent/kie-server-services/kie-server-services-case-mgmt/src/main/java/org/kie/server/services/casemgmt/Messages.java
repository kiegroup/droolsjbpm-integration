package org.kie.server.services.casemgmt;

final class Messages {

    static final String ILLEGAL_PAGE = "Page number cannot be negative, but was \"{0}\"";

    static final String ILLEGAL_PAGE_SIZE = "Page size cannot be negative, but was \"{0}\"";

    private Messages() {
        throw new UnsupportedOperationException("This class should not be instantiated.");
    }
}
