package org.kie.kproject.util;

import java.math.BigDecimal;
import java.math.MathContext;

public class Utils {
    public static BigDecimal b(double n) {
        return new BigDecimal(n, MathContext.DECIMAL128);
    }
}
