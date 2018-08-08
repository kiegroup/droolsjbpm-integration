package com.jboss.soap.service.acmedemo.impl;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdentifierGenerator {

    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
}
